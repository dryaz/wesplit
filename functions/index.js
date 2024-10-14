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

// Function to get service account credentials from Secret Manager
async function getServiceAccountCredentials() {
  const secretClient = new SecretManagerServiceClient();
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
