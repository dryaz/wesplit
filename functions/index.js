const functions = require('firebase-functions');
const admin = require('firebase-admin');
const axios = require('axios');
const { getFirestore } = require('firebase-admin/firestore');

admin.initializeApp();

const db = getFirestore();

exports.generateGroupToken = functions.https.onCall(async (data, context) => {
  const { groupId, publicToken } = data;

  if (!groupId || !publicToken) {
    throw new functions.https.HttpsError(
      'invalid-argument',
      'The function must be called with both "groupId" and "publicToken" arguments.'
    );
  }

  try {
    const groupDoc = await admin.firestore().collection('groups').doc(groupId).get();

    if (!groupDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Group not found.');
    }

    const groupData = groupDoc.data();

    // Hash the provided publicToken
    // const hashedPublicToken = crypto.createHash('sha256').update(publicToken).digest('hex');

    // Verify the hashed token matches
    if (groupData.publicToken !== publicToken) {
      throw new functions.https.HttpsError('permission-denied', 'Invalid public token.');
    }

    // Check if the public token has expired
    // const currentTime = admin.firestore.Timestamp.now();
    // if (groupData.publicTokenExpiration && currentTime > groupData.publicTokenExpiration) {
    //   throw new functions.https.HttpsError('permission-denied', 'Public token has expired.');
    // }

    // Optionally invalidate the token to make it single-use
    // await groupDoc.ref.update({ publicTokenHash: null, publicTokenExpiration: null });

    const customClaims = {
      groupId: groupId,
    };

    const uid = `group_${groupId}`;

    const customToken = await admin.auth().createCustomToken(uid, customClaims);

    return { customToken };
  } catch (error) {
    console.error('Error generating custom token:', error);

    if (error instanceof functions.https.HttpsError) {
      throw error;
    } else {
      throw new functions.https.HttpsError('internal', 'Unable to generate custom token.');
    }
  }
});

// Cloud Function to recalculate balances and update lastExpenseAt
exports.recalculateBalances = functions.firestore
  .document('groups/{groupId}/expenses/{expenseId}')
  .onWrite(async (change, context) => {
    const groupId = context.params.groupId;
    const groupRef = admin.firestore().collection('groups').doc(groupId);

    // Initialize balances map
    let balancesMap = {};

    // Initialize undistributed amounts map
    let undistributedMap = {};

    // Query expenses where status == 'NEW' or status == null
    const expensesQuery = groupRef.collection('expenses').where('status', '==', 'NEW');

    // Fetch all expenses in the group's expenses subcollection
    const expensesSnapshot = await expensesQuery.get();


    // Iterate over each expense to update balances
    expensesSnapshot.forEach(expenseDoc => {
      const expenseData = expenseDoc.data();
      const payedBy = expenseData.payedBy; // Participant object with 'id' and 'name'
      const amount = expenseData.totalAmount;   // Amount object with 'value' and 'currencyCode'
      const shares = expenseData.shares;   // Set of Share objects

      // Validate 'payedBy' and 'amount'
      if (!payedBy || !payedBy.id || !payedBy.name || !amount || typeof amount.value !== 'number' || !amount.currency) {
        console.error(`Expense ${expenseDoc.id} has invalid 'payedBy' or 'amount' data.`);
        return;
      }

      const currency = amount.currency;
      const totalPaid = amount.value;

      // Initialize balances for payer if not already done
      const payerId = payedBy.id;
      if (!balancesMap[payerId]) {
        balancesMap[payerId] = {
          participant: payedBy,
          amountsMap: {}
        };
      }
      if (!balancesMap[payerId].amountsMap[currency]) {
        balancesMap[payerId].amountsMap[currency] = 0;
      }

      // Credit the payer with the total amount paid
      balancesMap[payerId].amountsMap[currency] += totalPaid;

      // Initialize sum of shares
      let sumOfShares = 0;

      // Process shares
      shares.forEach(share => {
        const participant = share.participant; // Participant object
        const shareAmount = share.amount;      // Amount object with 'value' and 'currencyCode'

        // Validate participant and shareAmount
        if (!participant || !participant.id || !participant.name || !shareAmount || typeof shareAmount.value !== 'number' || !shareAmount.currency) {
          console.error(`Share in expense ${expenseDoc.id} has invalid participant or amount data.`);
          return;
        }

        if (shareAmount.currency !== currency) {
          console.error(`Currency mismatch in expense ${expenseDoc.id}.`);
          return;
        }

        const participantId = participant.id;
        const shareValue = shareAmount.value;

        // Initialize balances for participant if not already done
        if (!balancesMap[participantId]) {
          balancesMap[participantId] = {
            participant: participant,
            amountsMap: {}
          };
        }
        if (!balancesMap[participantId].amountsMap[currency]) {
          balancesMap[participantId].amountsMap[currency] = 0;
        }

        // Debit the participant by their share amount
        balancesMap[participantId].amountsMap[currency] -= shareValue;

        // Accumulate the sum of shares
        sumOfShares += shareValue;
      });

      // Calculate undistributed amount
      const undistributedAmount = totalPaid - sumOfShares;

      if (undistributedAmount !== 0) {
        // Initialize undistributed amount for this currency if not already done
        if (!undistributedMap[currency]) {
          undistributedMap[currency] = 0;
        }
        // Add undistributed amount to the total for this currency
        undistributedMap[currency] += undistributedAmount;
      }
    });

    // Construct participantsBalance set
    const participantsBalanceSet = Object.values(balancesMap).map(entry => {
      // Convert amountsMap to a set of Amount objects
      const amountsSet = Object.entries(entry.amountsMap).map(([currency, value]) => ({
        currency,
        value
      }));

      return {
        participant: entry.participant,
        amounts: amountsSet
      };
    });

    // Construct undistributed set
    const undistributedSet = Object.entries(undistributedMap).map(([currency, value]) => ({
      currency,
      value
    }));

    // Create the Balance object
    const balanceObject = {
      participantsBalance: participantsBalanceSet,
      undistributed: undistributedSet
    };

    // Prepare the update data
    const updateData = {
      balances: balanceObject, // Set containing one Balance object
      lastExpenseAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    // Update the group document with the recalculated balances and lastExpenseAt
    await groupRef.update(updateData);
  });

exports.updateCurrencyRates = functions.pubsub.schedule('0 0 * * *').timeZone('UTC').onRun(async (context) => {
  try {
    const apiKey = functions.config().exchangerate.key;
    const baseUrl = `https://v6.exchangerate-api.com/v6/${apiKey}/latest/USD`;

    // Fetch exchange rates
    const response = await axios.get(baseUrl);
    const data = response.data;

    if (data.result === 'success') {
      // Write data to Firestore
      const timestamp = admin.firestore.FieldValue.serverTimestamp();

      await db.collection('fxrates').doc('latest').set({
        base: data.base_code,
        rates: data.conversion_rates,
        lastUpdate: timestamp,
      });

      const strTimestamp = new Date().toISOString().replace(/[:.]/g, '-');
      await db.collection('fxrates').doc(strTimestamp).set({
        base: data.base_code,
        rates: data.conversion_rates,
        lastUpdate: timestamp,
      });

      console.log('Exchange rates updated successfully.');
    } else {
      console.error('Error fetching exchange rates:', data['error-type']);
    }
  } catch (error) {
    console.error('Error in updateCurrencyRates function:', error);
  }
});

// Function to update expenses with null or missing status
async function updateExpensesStatus(req, res) {
  try {

    // Proceed with updating expenses
    const groupsSnapshot = await admin.firestore().collection('groups').get();

    for (const groupDoc of groupsSnapshot.docs) {
      const groupRef = groupDoc.ref;
      const expensesSnapshot = await groupRef.collection('expenses').get();

      for (const expenseDoc of expensesSnapshot.docs) {
        await expenseDoc.ref.update({ status: 'NEW' });
        console.log(`Updated expense ${expenseDoc.id} with status 'NEW'.`);
      }
    }

    res.status(200).send('Expenses status updated successfully.');
  } catch (error) {
    console.error('Error updating expenses status:', error);
    res.status(500).send('An error occurred while updating expenses status.');
  }
}

// Export the function as an HTTPS function
exports.updateExpensesStatus = functions.https.onRequest(updateExpensesStatus);

// Import necessary modules
const { onMessagePublished } = require('firebase-functions/v2/pubsub');
const logger = require('firebase-functions/logger');
const { google } = require('googleapis');
const { SecretManagerServiceClient } = require('@google-cloud/secret-manager');
const { GoogleAuth } = require('google-auth-library');

const secretClient = new SecretManagerServiceClient();

// Function to get service account credentials from Secret Manager
async function getServiceAccountCredentials() {
  const [version] = await secretClient.accessSecretVersion({
    name: 'projects/548791587175/secrets/play-in-app/versions/latest',
  });
  const keyFileContent = version.payload.data.toString('utf8');
  return JSON.parse(keyFileContent);
}

exports.handleSubscriptionUpdates = onMessagePublished('in-app-android', async (event) => {
  const pubsubMessage = event.data.message;
  const data = pubsubMessage.json;

  if (data) {
    const notification = data;
    const { subscriptionNotification } = notification;
    const { purchaseToken, subscriptionId } = subscriptionNotification;

    // Authenticate with Google Play Developer API
    const keyFileObject = await getServiceAccountCredentials();
    const auth = new GoogleAuth({
      credentials: keyFileObject,
      scopes: ['https://www.googleapis.com/auth/androidpublisher'],
    });

    const authClient = await auth.getClient();

    const publisher = google.androidpublisher({
      version: 'v3',
      auth: authClient,
    });

    const packageName = 'app.wesplit'; // Replace with your app's package name

    try {
      // Verify the purchase with Google Play Developer API
      const res = await publisher.purchases.subscriptions.get({
        packageName,
        subscriptionId,
        token: purchaseToken,
      });

      const purchase = res.data;

      // Access obfuscatedExternalAccountId
      const userId = purchase.obfuscatedExternalAccountId;

      if (userId) {
        // Use obfuscatedAccountId to find the user
        const usersSnapshot = await db.collection('users').doc(userId).get();

        if (!usersSnapshot.empty) {
          // Acknowledge the purchase if not already acknowledged
          if (purchase.acknowledgementState === 0) {
            await publisher.purchases.subscriptions.acknowledge({
              packageName,
              subscriptionId,
              token: purchaseToken,
              requestBody: {
                // Optional developer payload you can use to track this acknowledgement
                developerPayload: 'Acknowledged via Cloud Function',
              },
            });
            console.log('Subscription purchase acknowledged.');
          } else {
            console.log('Subscription purchase already acknowledged.');
          }

          // Determine subscription status
          const currentTime = Date.now();
          const expiryTime = parseInt(purchase.expiryTimeMillis);

          let subsStatus = 'basic';
          if (
            purchase.paymentState === 1 && // Payment received
            expiryTime > currentTime &&    // Subscription has not expired
            purchase.cancelReason == null  // No cancellation
          ) {
            subsStatus = 'plus';
          }

          await db.collection('users').doc(userId).update({
            subs: subsStatus,
          });

          logger.info(`Subscription status updated for user: ${userId} to ${subsStatus}`);
        } else {
          logger.error('No user found for obfuscatedAccountId:', userId);
        }
      } else {
        logger.error('obfuscatedExternalAccountId not found in purchase data');
      }
    } catch (error) {
      logger.error('Error verifying subscription:', {
        message: error.message,
        code: error.code,
        errors: error.errors,
        response: error.response ? error.response.data : null,
      });
    }
  } else {
    logger.error('Pub/Sub message data is empty');
  }
});

// Apple supscriptions

const { onRequest } = require('firebase-functions/v2/https');
const { SignedDataVerifier, Environment } = require('@apple/app-store-server-library');

// List of Apple CA secrets in Secret Manager
const appleCaSecrets = [
  'projects/548791587175/secrets/apple-ca-1/versions/latest',
  'projects/548791587175/secrets/apple-ca-2/versions/latest',
  'projects/548791587175/secrets/apple-ca-3/versions/latest',
  'projects/548791587175/secrets/apple-ca-4/versions/latest',
];

// Function to convert DER (binary) to PEM format
function derToPem(derBuffer) {
  const base64Cert = derBuffer.toString('base64');
  // Insert line breaks every 64 characters
  const pemCert = `-----BEGIN CERTIFICATE-----\n${base64Cert.match(/.{1,64}/g).join('\n')}\n-----END CERTIFICATE-----\n`;
  return pemCert;
}

// Function to load and convert all Apple root certificates from Secret Manager
async function loadAppleRootCAs() {
  try {
    const certificatePromises = appleCaSecrets.map(async (secretName) => {
      const [version] = await secretClient.accessSecretVersion({ name: secretName });
      const payload = version.payload.data;

      // Convert binary DER to PEM
      const pemCert = derToPem(payload);

      // Validate PEM format
      if (!pemCert.startsWith('-----BEGIN CERTIFICATE-----') || !pemCert.endsWith('-----END CERTIFICATE-----\n')) {
        throw new Error(`Certificate fetched from ${secretName} is not in valid PEM format after conversion.`);
      }

      return pemCert;
    });

    const appleRootCAs = await Promise.all(certificatePromises);
    return appleRootCAs;
  } catch (error) {
    console.error('Error loading Apple Root CAs:', error);
    throw new Error('Failed to load and convert Apple Root Certificates');
  }
}

// Global variable to cache the verifier
let signedDataVerifier;

// Function to initialize the SignedDataVerifier
async function initializeVerifier() {
  if (!signedDataVerifier) {
    // Load Apple Root CAs from Secret Manager
    const appleRootCAs = await loadAppleRootCAs();

    // Configuration parameters
    const bundleId = 'app.wesplit.ios'; // Replace with your app's bundle ID
    const enableOnlineChecks = true; // Enable CRL and OCSP checks
    const environment = Environment.SANDBOX; // Use Environment.SANDBOX for sandbox or PRODUCTION for prod
    const appAppleId = '6714482007'; // Replace with your app's Apple ID if in production

    // Initialize SignedDataVerifier with the required parameters
    signedDataVerifier = new SignedDataVerifier(
      appleRootCAs,
      enableOnlineChecks,
      environment,
      bundleId,
      appAppleId
    );

    console.log('SignedDataVerifier initialized successfully.');
  }
}

// Firebase Cloud Function to handle App Store Server Notifications
exports.handleAppleServerNotification = onRequest(async (req, res) => {
  try {
    // Initialize the verifier if not already done
    await initializeVerifier();

    const { signedPayload } = req.body;
    if (!signedPayload) {
      res.status(400).send('Missing signedPayload');
      return;
    }

    // Verify and decode the notification
    let decodedPayload;
    try {
      decodedPayload = await signedDataVerifier.verifyAndDecodeNotification(signedPayload);
    } catch (error) {
      console.error('Failed to verify and decode notification:', error);
      res.status(400).send('Invalid notification signature');
      return;
    }

    // The notification is now verified and decoded
    const notificationType = decodedPayload.notificationType;
    const data = decodedPayload.data;

    // Verify and decode the signedTransactionInfo
    const signedTransactionInfo = data.signedTransactionInfo;
    let transactionInfo;
    if (signedTransactionInfo) {
      try {
        transactionInfo = await signedDataVerifier.verifyAndDecodeTransaction(signedTransactionInfo);
      } catch (error) {
        console.error('Failed to verify and decode transaction info:', error);
        res.status(400).send('Invalid transaction info signature');
        return;
      }
    } else {
      console.error('Missing signedTransactionInfo');
      res.status(400).send('Missing transaction info');
      return;
    }

    const transactionId = transactionInfo.transactionId;
    const originalTransactionId = transactionInfo.originalTransactionId;
    const productId = transactionInfo.productId;

    // Map productId to plan
    const productIdToPlan = {
      'week': 'plus',
      'month': 'plus',
      'year': 'plus',
    };

    const plan = productIdToPlan[productId];
    if (!plan) {
      console.error('Unknown productId:', productId);
      res.status(400).send('Unknown productId');
      return;
    }

    // Query Firestore using 'trxId' which corresponds to 'originalTransactionId'
    const userSnapshot = await admin.firestore().collection('users')
      .where('trxId', '==', originalTransactionId)
      .limit(1)
      .get();

    if (userSnapshot.empty) {
      console.error('(notificationType) | No user found with trxId (originalTransactionId):', notificationType, originalTransactionId);
      res.status(400).send('User not found');
      return;
    }

    const userDoc = userSnapshot.docs[0];
    const userId = userDoc.id;

    // Reference to the user's subscription document
    const subscriptionRef = admin.firestore().collection('users').doc(userId);

    // Fetch the current subscription status
    const subscriptionDoc = await subscriptionRef.get();
    if (subscriptionDoc.exists) {
      const subscriptionData = subscriptionDoc.data();

    // Check if this transaction has already been processed
    if (subscriptionData.lastTransactionId === transactionId) {
       console.log(`(${notificationType}) Notification for transactionId ${transactionId} has already been processed.`);
     }
   }

    // Update Firestore based on notification type with idempotency
    switch (notificationType) {
      case 'SUBSCRIBED':
        // Activate the subscription
        await subscriptionRef.update({
          subs: plan,
          subscriptionStatus: 'active',
          lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
          lastTransactionId: transactionId,
        });
        console.log(`Subscription activated for user ${userId}.`);
        break;

      case 'DID_RENEW':
        // Extend the subscription period and set status to active
        await subscriptionRef.update({
          subs: plan,
          subscriptionStatus: 'active',
          lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
          lastTransactionId: transactionId,
        });
        console.log(`Subscription renewed for user ${userId}.`);
        break;

      case 'EXPIRED':
        // Set subscription status to inactive
        await subscriptionRef.update({
          subs: null,
          subscriptionStatus: 'expired',
          lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
          lastTransactionId: transactionId,
        });
        console.log(`Subscription expired for user ${userId}.`);
        break;

      case 'DID_FAIL_TO_RENEW':
        // Notify user and set status to indicate renewal failure
        await subscriptionRef.update({
          subs: null,
          subscriptionStatus: 'failed_to_renew',
          lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
          lastTransactionId: transactionId,
        });
        console.log(`Subscription failed to renew for user ${userId}.`);
        // Optionally, send an in-app notification or email to the user
        break;

      case 'GRACE_PERIOD_EXPIRED':
        // Set subscription status to inactive after grace period
        await subscriptionRef.update({
          subs: null,
          subscriptionStatus: 'grace_period_expired',
          lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
          lastTransactionId: transactionId,
        });
        console.log(`Grace period expired for user ${userId}.`);
        break;

      case 'PRICE_INCREASE':
        // Notify user about price increase
        // Optionally, update pricing details in Firestore
        console.log(`Price increased for user ${userId}.`);
        // Implement notification logic as needed
        break;

      case 'REFUND':
        // Revoke subscription benefits and notify the user
        await subscriptionRef.update({
          subs: null,
          subscriptionStatus: 'refunded',
          lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
          lastTransactionId: transactionId,
        });
        console.log(`Subscription refunded for user ${userId}.`);
        // Optionally, notify the user about the refund
        break;

      case 'REFUND_DECLINED':
        // Log the declined refund attempt
        console.log(`Refund declined for user ${userId}.`);
        // Optionally, notify the user about the declined refund
        break;

      case 'RENEWAL_EXTENDED':
      case 'RENEWAL_EXTENSION':
        // Extend the subscription renewal period
        await subscriptionRef.update({
          subs: plan,
          lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
          lastTransactionId: transactionId,
        });
        console.log(`Renewal extended for user ${userId}.`);
        break;

      case 'REVOKE':
        // Revoke the subscription entirely
        await subscriptionRef.update({
          subs: null,
          subscriptionStatus: 'revoked',
          lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
          lastTransactionId: transactionId,
        });
        console.log(`Subscription revoked for user ${userId}.`);
        break;

      case 'TEST':
        // Log the receipt of a test notification
        console.log(`Test notification received for user ${userId}.`);
        // Optionally, perform test-specific actions
        break;

      case 'REFUND_REVERSED':
        // Reinstate subscription benefits and notify the user
        await subscriptionRef.update({
          subs: plan,
          subscriptionStatus: 'active',
          lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
          lastTransactionId: transactionId,
        });
        console.log(`Refund reversed and subscription reinstated for user ${userId}.`);
        break;

      default:
        console.warn('Unhandled notification type:', notificationType);
        // Optionally, log this event for further analysis
        break;
    }

    res.status(200).send('Notification processed and user updated');
  } catch (error) {
    console.error('Error processing notification:', error);
    res.status(500).send('Error processing notification');
  }
});
