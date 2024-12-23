const functions = require('firebase-functions');
const admin = require('firebase-admin');
const axios = require('axios');
const { getFirestore, Timestamp } = require('firebase-admin/firestore');

const { onCall } = require("firebase-functions/v2/https");
const { getAuth } = require("firebase-admin/auth");
const { HttpsError } = require("firebase-functions/v2");
const crypto = require("crypto");

const { v4: uuidv4 } = require("uuid");

const { getRemoteConfig } = require("firebase-admin/remote-config");

const firebaseApp = admin.initializeApp();

const db = getFirestore();
const auth = getAuth();

// Import required modules
const { onDocumentWritten } = require('firebase-functions/v2/firestore');

const { defineSecret } = require('firebase-functions/params');
const OPENAI_API_KEY = defineSecret('OPENAI_API_KEY');

const STORAGE_BUCKET = "wesplit-bill.appspot.com"; // Your Firebase Storage bucket name

exports.handleImageGeneration = onDocumentWritten(
  {
    document: "groups/{groupId}",
    region: "us-central1", // Adjust region as needed
    secrets: [OPENAI_API_KEY], // Include the secret
  },
  async (event) => {
    const beforeData = event.data.before ? event.data.before.data() : null;
    const afterData = event.data.after ? event.data.after.data() : null;
    const groupId = event.params.groupId;

    if (!afterData) {
      // Document deleted, do nothing
      return;
    }

    // Check if imageDescription has changed
    const newImageDescription = afterData.imageDescription;
    const oldImageDescription = beforeData ? beforeData.imageDescription : null;

    if (newImageDescription === oldImageDescription || !newImageDescription) {
      // No change in imageDescription or no description provided
      return;
    }

    try {
      // Fetch the image resolution from Remote Config
      const template = await getRemoteConfig(firebaseApp).getServerTemplate();
      const config = template.evaluate()

      const imageResolution = config.getString('image_res') || "1024x1024";
      const imageQuality = config.getString('image_quality') || "hd";
      const imageModel = config.getString('image_model') || "dall-e-3";
      const imageStyle = config.getString('image_style') || "vivid";
      const imagePromptTemplate = config.getString('image_prompt') || "Generate a square flat vector-style icon about \"{description}\". The icon should look visually appealing and recognizable even at 128x128 resolution. No borders, paddings, or text.";

      console.log(`Generating image for group: ${groupId} | ${newImageDescription} | ${imageModel}, ${imageQuality}, ${imageResolution}, ${imageStyle}`);

      // Step 1: Craft the prompt
      const prompt = imagePromptTemplate.replace("{description}", newImageDescription);


      // Step 2: Generate image via OpenAI API
      const openaiResponse = await axios.post(
        "https://api.openai.com/v1/images/generations",
        {
          prompt: prompt,
          n: 1,
          model: imageModel,
          quality: imageQuality,
          size: imageResolution,
          style: imageStyle,
        },
        {
          headers: {
            Authorization: `Bearer ${OPENAI_API_KEY.value()}`, // Use the secret value
            "Content-Type": "application/json",
          },
        }
      );

      const imageUrl = openaiResponse.data.data[0].url;

      // Step 2: Download the generated image
      const imageResponse = await axios.get(imageUrl, { responseType: "arraybuffer" });
      const imageBuffer = Buffer.from(imageResponse.data, "binary");

      // Step 3: Upload image to Firebase Storage
      const timestamp = Date.now();
      const fileName = `${groupId}/${timestamp}.png`;
      const bucket = admin.storage().bucket(STORAGE_BUCKET);
      const file = bucket.file(fileName);

      const downloadToken = uuidv4(); // Generate a unique token for accessing the file

      await file.save(imageBuffer, {
        metadata: {
          contentType: "image/png",
          metadata: {
            firebaseStorageDownloadTokens: downloadToken, // Attach the token
          },
        },
      });

      // Construct the URL in the desired format
      const publicUrl = `https://firebasestorage.googleapis.com/v0/b/${STORAGE_BUCKET}/o/${encodeURIComponent(fileName)}?alt=media&token=${downloadToken}`;

      console.log(`Image uploaded successfully: ${publicUrl}`);

      // Step 4: Update Firestore with the new image URL
      await event.data.after.ref.update({
        imageUrl: publicUrl,
        isImageGen: false,
      });

      console.log(`Updated Firestore document for group: ${groupId}`);
    } catch (error) {
      console.error(`Error generating or uploading image for group: ${groupId}`, error);
    }
  }
);

// Define the category mapping
const categoryMapping = {
  0: "None",
  1: "Housing",
  2: "Utilities",
  3: "Electricity",
  4: "Internet",
  5: "Water",
  6: "Recycling",
  7: "Garbage",
  8: "Housing/Repair",
  9: "Cleaning",
  10: "Rent",
  11: "Tax",
  12: "Furnishing",
  13: "Security",
  14: "Food and Drink",
  15: "Fast Food",
  16: "Coffee",
  17: "Restaurant",
  18: "Groceries",
  19: "Transport and Travel",
  20: "Transportation",
  21: "Taxi",
  22: "Flight",
  23: "Public",
  24: "Car",
  25: "Parking",
  26: "Tolls",
  27: "Fee",
  28: "Gifts",
  29: "Shopping",
  30: "Technology",
  31: "Clothes",
  32: "Shoes",
  33: "Entertainment",
  34: "Movie",
  35: "Concert",
  36: "Books",
  37: "Sport Event",
  38: "Hobby",
  39: "Health and Beauty",
  40: "Health",
  41: "Beauty",
  42: "Sport",
  43: "Money Transfer",
  44: "Cash",
  45: "Bank Transfer",
  46: "Crypto",
};

// Define the function using Firebase Functions v2 syntax
exports.assignCategoryOnExpenseWrite = onDocumentWritten(
  {
    document: 'groups/{groupId}/expenses/{expenseId}',
    region: 'us-central1', // Specify your desired region
    secrets: [OPENAI_API_KEY], // Include the secret
  },
  async (event) => {
    const { groupId } = event.params;

    const beforeData = event.data.before ? event.data.before.data() : null;
    const afterData = event.data.after ? event.data.after.data() : null;

    // If the document was deleted, exit
    if (!afterData) {
      console.log('Document was deleted, no action needed.');
      return;
    }

    // Check if 'cat' field is -1
    if (afterData.cat !== "-1") {
      console.log("'cat' field is not -1, no action needed.");
      return;
    }

    // Prevent infinite loops
    if (beforeData && beforeData.cat === afterData.cat) {
      console.log("'cat' field hasn't changed, no action needed.");
      return;
    }

    const title = afterData.title;

    try {
      // Prepare messages for Chat Completion API
      const messages = [
        {
          role: 'system',
          content: `You are an assistant that assigns a category number to expenses based on their title.

Here is a mapping of categories:

${Object.entries(categoryMapping)
  .map(([key, value]) => `${key}: ${value}`)
  .join('\n')}

Given an expense title, determine the closest category number from the mapping. If the title is unclear or does not match any category, respond with "0" (None). Provide only the category number as the answer.`,
        },
        {
          role: 'user',
          content: `Title: "${title}"\nCategory number:`,
        },
      ];

      // Call OpenAI Chat Completion API
      const response = await axios.post(
        'https://api.openai.com/v1/chat/completions',
        {
          model: 'gpt-4o-mini', // Use 'gpt-4' or 'gpt-3.5-turbo' or 'gpt-4o-mini' model
          messages: messages,
          max_tokens: 10,
          temperature: 0,
        },
        {
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${OPENAI_API_KEY.value()}`, // Use the secret value
          },
        }
      );

      const result = response.data.choices[0].message.content.trim();
      console.log(`Title: ${title} | Get response from AI: ${result}`);
      const category = parseInt(result, 10);

      if (!isNaN(category)) {
        // Update the 'cat' field in Firestore
        await event.data.after.ref.update({ cat: category.toString() });
        console.log(`Updated 'cat' to ${category} for expense: ${title}`);
      } else {
      await event.data.after.ref.update({ cat: "0" });
        console.error("Failed to parse 'cat' from AI response:", result);
      }
    } catch (error) {
      console.error('Error assigning category:', error.response?.data || error.message);
    }
  }
);

exports.generateGroupToken = onCall(async (request) => {
  const { groupId, publicToken } = request.data;

  // Validate input parameters
  if (!groupId || !publicToken) {
    throw new HttpsError(
      "invalid-argument",
      'The function must be called with both "groupId" and "publicToken" arguments.'
    );
  }

  try {
    const groupDoc = await db.collection("groups").doc(groupId).get();

    // Check if the group exists
    if (!groupDoc.exists) {
      throw new HttpsError("not-found", "Group not found.");
    }

    const groupData = groupDoc.data();

    // Hash the provided publicToken
    // const hashedPublicToken = crypto.createHash("sha256").update(publicToken).digest("hex");

    // Verify the hashed token matches
    if (groupData.publicToken !== publicToken) {
      throw new HttpsError("permission-denied", "Invalid public token.");
    }

    // Check if the public token has expired
    const currentTime = Timestamp.now();
    if (groupData.publicTokenExpiration && currentTime > groupData.publicTokenExpiration) {
      throw new HttpsError("permission-denied", "Public token has expired.");
    }

    // Optionally invalidate the token to make it single-use
    // await groupDoc.ref.update({ publicToken: null, publicTokenExpiration: null });

    const customClaims = {
      groupId: groupId,
    };

    const uid = `group_${groupId}`;
    const customToken = await auth.createCustomToken(uid, customClaims);

    return { customToken };
  } catch (error) {
    console.error("Error generating custom token:", error);

    if (error instanceof HttpsError) {
      throw error;
    } else {
      throw new HttpsError("internal", "Unable to generate custom token.");
    }
  }
});

// Dedicated function to send notifications when a new expense is created
exports.notificationOnNewExpense = functions.firestore
  .document("groups/{groupId}/expenses/{expenseId}")
  .onCreate(async (snapshot, context) => {
    const expense = snapshot.data();
    const groupId = context.params.groupId;

    if (!expense || !groupId) {
      console.error("Expense data or Group ID is missing");
      return;
    }

    console.log("New expense created, notify users");
    await sendNotifications(expense, groupId);
  });

// Helper function to send notifications
async function sendNotifications(expense, groupId) {
  const title = `${expense.title}: ${expense.totalAmount.currency} ${expense.totalAmount.value}`;

  console.log("Preparing notifications for each participant in the expense");

  for (const share of expense.shares) {
    const participant = share.participant;

    if (!participant || !participant.user || !participant.user.authIds || participant.user.authIds.length === 0) {
      console.warn("Skipping participant with missing authIds");
      continue; // If authIds is missing or empty, skip this participant
    }

    // Get the first authId
    const userId = participant.user.authIds[0];
    const userRef = admin.firestore().collection("users").doc(userId);
    const userDoc = await userRef.get();

    if (!userDoc.exists) {
      console.warn(`User document for ID ${userId} does not exist`);
      continue;
    }

    const userData = userDoc.data();
    if (!userData.fcm) {
      console.warn(`No FCM tokens found for user ID ${userId}`);
      continue;
    }

    const tokens = Array.from(userData.fcm);
    const message = {
      notification: {
        title: title,
        body: `Your share: ${share.amount.currency} ${share.amount.value.toFixed(2)}`,
      },
      android: {
        notification: {
          channelId: "expense_added",
        },
      },
      data: {
        groupId: String(groupId),
        expenseId: String(expense.id),
        url: `https://web.wesplit.app/group/${groupId}`, // URL is already a string
      },
      topic: "expense_added",
    };

    console.log(`Sending notification to user ID ${userId} with tokens`, tokens);

    try {
      const response = await admin.messaging().sendEachForMulticast({
        tokens: tokens,
        ...message,
      });

      for (let i = 0; i < response.responses.length; i++) {
        const resp = response.responses[i];
        if (!resp.success) {
          console.error(`Error sending to token ${tokens[i]}:`, resp.error);
          if (
            resp.error.code === "messaging/invalid-registration-token" ||
            resp.error.code === "messaging/registration-token-not-registered"
          ) {
            console.log(`Removing invalid token for user ID ${userId}`);
            await userRef.update({
              fcm: admin.firestore.FieldValue.arrayRemove(tokens[i]),
            });
          }
        } else {
          console.log(`Notification sent successfully to token ${tokens[i]}`);
        }
      }
    } catch (error) {
      console.error(`Error sending notification to user ID ${userId}:`, error);
    }
  }
}

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

    // After recalculating balances, send notifications for new expense
//    if (!change.before.exists) { // Only send if it's a new expense (not an update or deletion)
//      const expense = change.after.data();
//      console.log(`Send notifications for new expense`);
//      await sendNotifications(expense, groupId);
//    }
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

          let expiresAt;

          let subsStatus = 'basic';
          if (
            purchase.paymentState === 1 && // Payment received
            expiryTime > currentTime &&    // Subscription has not expired
            purchase.cancelReason == null  // No cancellation
          ) {
            if (expiryTime) {
               // Ensure that expiresDate is in milliseconds. If it's in seconds, multiply by 1000.
               expiresAt = admin.firestore.Timestamp.fromMillis(expiryTime);
            }
            subsStatus = 'plus';
          }

          console.log(`purchase.paymentState: ${purchase.paymentState}`);
          console.log(`expiryTime: ${expiryTime}`);
          console.log(`currentTime: ${currentTime}`);
          console.log(`purchase.cancelReason: ${purchase.cancelReason}`);
          await db.collection('users').doc(userId).update({
            expiresAt: expiresAt || admin.firestore.FieldValue.delete(),
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
  'projects/548791587175/secrets/apple-ca-3/versions/latest',
  'projects/548791587175/secrets/apple-ca-4/versions/latest',
  'projects/548791587175/secrets/apple-ca-1/versions/latest',
  'projects/548791587175/secrets/apple-ca-2/versions/latest',
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
let signedDataVerifierDev;

// Function to initialize the SignedDataVerifier
async function initializeVerifier() {
  if (!signedDataVerifier) {
    // Load Apple Root CAs from Secret Manager
    const appleRootCAs = await loadAppleRootCAs();

    // Configuration parameters
    const bundleId = 'app.wesplit.ios'; // Replace with your app's bundle ID
    const enableOnlineChecks = true; // Enable CRL and OCSP checks
    const environment = Environment.PRODUCTION; // Use Environment.SANDBOX for sandbox or PRODUCTION for prod
    const appAppleId = 6714482007; // Replace with your app's Apple ID if in production

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

// Function to initialize the SignedDataVerifier
async function initializeVerifierDev() {
  if (!signedDataVerifierDev) {
    // Load Apple Root CAs from Secret Manager
    const appleRootCAs = await loadAppleRootCAs();

    // Configuration parameters
    const bundleId = 'app.wesplit.ios'; // Replace with your app's bundle ID
    const enableOnlineChecks = true; // Enable CRL and OCSP checks
    const environment = Environment.SANDBOX; // Use Environment.SANDBOX for sandbox or PRODUCTION for prod
    const appAppleId = undefined; // Replace with your app's Apple ID if in production

    // Initialize SignedDataVerifier with the required parameters
    signedDataVerifierDev = new SignedDataVerifier(
      appleRootCAs,
      enableOnlineChecks,
      environment,
      bundleId,
      appAppleId
    );

    console.log('SignedDataVerifier Dev initialized successfully.');
  }
}

// Firebase Cloud Function to handle App Store Server Notifications
exports.handleAppleServerNotificationDev = onRequest(async (req, res) => {
  try {
    // Initialize the verifier if not already done
    await initializeVerifierDev();

    const { signedPayload } = req.body;
    if (!signedPayload) {
      res.status(400).send('Missing signedPayload');
      return;
    }

    // Verify and decode the notification
    let decodedPayload;
    try {
      decodedPayload = await signedDataVerifierDev.verifyAndDecodeNotification(signedPayload);
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
        transactionInfo = await signedDataVerifierDev.verifyAndDecodeTransaction(signedTransactionInfo);
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
    const expiresDate = transactionInfo.expiresDate;

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

   // Convert expiresDate to Firestore Timestamp if it exists
   let expiresAt;
   if (expiresDate) {
     // Ensure that expiresDate is in milliseconds. If it's in seconds, multiply by 1000.
     expiresAt = admin.firestore.Timestamp.fromMillis(expiresDate);
   }

    // Update Firestore based on notification type with idempotency
    switch (notificationType) {
      case 'SUBSCRIBED':
        // Activate the subscription
        await subscriptionRef.update({
          subs: plan,
          subscriptionStatus: 'active',
          expiresAt: expiresAt || admin.firestore.FieldValue.delete(),
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
          expiresAt: expiresAt || admin.firestore.FieldValue.delete(),
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
          expiresAt: admin.firestore.FieldValue.delete(),
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
          expiresAt: admin.firestore.FieldValue.delete(),
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
          expiresAt: admin.firestore.FieldValue.delete(),
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
          expiresAt: admin.firestore.FieldValue.delete(),
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
          expiresAt: expiresAt || admin.firestore.FieldValue.delete(),
          lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
          lastTransactionId: transactionId,
        });
        console.log(`Renewal extended for user ${userId}.`);
        break;

      case 'REVOKE':
        // Revoke the subscription entirely
        await subscriptionRef.update({
          subs: null,
          expiresAt: admin.firestore.FieldValue.delete(),
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
          expiresAt: expiresAt || admin.firestore.FieldValue.delete(),
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
    const expiresDate = transactionInfo.expiresDate;

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

   // Convert expiresDate to Firestore Timestamp if it exists
   let expiresAt;
   if (expiresDate) {
     // Ensure that expiresDate is in milliseconds. If it's in seconds, multiply by 1000.
     expiresAt = admin.firestore.Timestamp.fromMillis(expiresDate);
   }

    // Update Firestore based on notification type with idempotency
    switch (notificationType) {
      case 'SUBSCRIBED':
        // Activate the subscription
        await subscriptionRef.update({
          subs: plan,
          subscriptionStatus: 'active',
          expiresAt: expiresAt || admin.firestore.FieldValue.delete(),
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
          expiresAt: expiresAt || admin.firestore.FieldValue.delete(),
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
          expiresAt: admin.firestore.FieldValue.delete(),
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
          expiresAt: admin.firestore.FieldValue.delete(),
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
          expiresAt: admin.firestore.FieldValue.delete(),
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
          expiresAt: admin.firestore.FieldValue.delete(),
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
          expiresAt: expiresAt || admin.firestore.FieldValue.delete(),
          lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
          lastTransactionId: transactionId,
        });
        console.log(`Renewal extended for user ${userId}.`);
        break;

      case 'REVOKE':
        // Revoke the subscription entirely
        await subscriptionRef.update({
          subs: null,
          expiresAt: admin.firestore.FieldValue.delete(),
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
          expiresAt: expiresAt || admin.firestore.FieldValue.delete(),
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

// Define the Cloud Function
exports.updateUserSubscription = functions.firestore
  .document('users/{userId}')
  .onCreate(async (snap, context) => {
    // Extract the user ID from the context parameters
    const userId = context.params.userId;

    // Reference to the newly created user document
    const userRef = admin.firestore().collection('users').doc(userId);

    // Current server timestamp
    const currentTime = Timestamp.now();

    // Fetch the image resolution from Remote Config
    const template = await getRemoteConfig(firebaseApp).getServerTemplate();
    const config = template.evaluate()

    const days = config.getNumber('free_trial_days') || 3;

    // Calculate the expiration time
    const trialDays = days * 24 * 60 * 60; // X days in seconds
    const expiresAt = Timestamp.fromMillis(currentTime.toMillis() + trialDays * 1000);

    try {
      // Update the 'subs' field to 'plus' and add 'expiresAt'
      await userRef.update({
        subs: 'plus',
        expiresAt: expiresAt,
        lastUpdate: admin.firestore.FieldValue.serverTimestamp(),
      });

      console.log(`User ${userId} subscription updated to 'plus' with expiration at ${expiresAt.toDate()}.`);
    } catch (error) {
      console.error(`Error updating subscription for user ${userId}:`, error);
    }
  });

/**
 * Callable Cloud Function to apply a promo code.
 *
 * Input:
 * - promo: string (the promo code to apply)
 *
 * Output:
 * - success: boolean
 * - message: string
 */
exports.applyPromoCode = functions.https.onCall(async (data, context) => {
    // 1. Authenticate the user
    if (!context.auth) {
        console.log('Unauthenticated access attempt to apply promo code.');
        throw new functions.https.HttpsError(
            'unauthenticated',
            'The function must be called while authenticated.'
        );
    }

    const uid = context.auth.uid;
    const promoCode = data.promo;

    if (!promoCode || typeof promoCode !== 'string') {
        console.log(`User ${uid} provided an invalid promo code.`);
        throw new functions.https.HttpsError(
            'invalid-argument',
            'The promo code must be a non-empty string.'
        );
    }

    console.log(`User ${uid} is attempting to apply promo code: ${promoCode}`);

    const promoRef = db.collection('promo').doc(promoCode);
    const userRef = db.collection('users').doc(uid);

    try {
        // 2. Fetch the promo document
        const promoDoc = await promoRef.get();
        if (!promoDoc.exists) {
            console.log(`Promo code "${promoCode}" does not exist. User: ${uid}`);
            throw new functions.https.HttpsError(
                'not-found',
                'Promo code does not exist.'
            );
        }

        const promoData = promoDoc.data();
        const promoDays = promoData.days;
        const promoExpiresAt = promoData.expiresAt;

        if (typeof promoDays !== 'number' || promoDays <= 0) {
            console.log(`Promo code "${promoCode}" has invalid "days" value. User: ${uid}`);
            throw new functions.https.HttpsError(
                'invalid-argument',
                'Promo code has invalid "days" value.'
            );
        }

        // 3. Run a transaction to ensure atomicity
        await db.runTransaction(async (transaction) => {
            const userDoc = await transaction.get(userRef);
            if (!userDoc.exists) {
                console.log(`User document for UID "${uid}" not found.`);
                throw new functions.https.HttpsError(
                    'invalid-data',
                    'Can\'t find user document with provided ID in auth header'
                );
            }

            const userData = userDoc.data();
            const appliedPromos = userData.appliedPromos || [];

            // 4. Check if the promo code has already been used
            if (appliedPromos.includes(promoCode)) {
                console.log(`User ${uid} has already used promo code "${promoCode}".`);
                throw new functions.https.HttpsError(
                    'already-exists',
                    'Promo code has already been used by this user.'
                );
            }

            // 5. Calculate the new expiresAt value
            let newExpiresAt;
            const now = admin.firestore.Timestamp.now();

            if (userData.expiresAt && userData.expiresAt.toMillis() > now.toMillis()) {
                // Extend the existing expiresAt
                newExpiresAt = admin.firestore.Timestamp.fromMillis(
                    userData.expiresAt.toMillis() + promoDays * 24 * 60 * 60 * 1000
                );
                console.log(`Extending subscription for user ${uid} by ${promoDays} days.`);
            } else {
                // Set expiresAt to now + promoDays
                newExpiresAt = admin.firestore.Timestamp.fromMillis(
                    now.toMillis() + promoDays * 24 * 60 * 60 * 1000
                );
                console.log(`Setting new subscription for user ${uid} for ${promoDays} days.`);
            }

            // 6. Update the user's document
            transaction.update(userRef, {
                subs: 'plus',
                expiresAt: newExpiresAt,
                appliedPromos: admin.firestore.FieldValue.arrayUnion(promoCode)
            });
        });

        // 7. Log success and return success response
        console.log(`Promo code "${promoCode}" applied successfully for user ${uid}.`);
        return {
            success: true,
            message: 'Promo code applied successfully.'
        };

    } catch (error) {
        // Handle known errors
        if (error instanceof functions.https.HttpsError) {
            // Log the error message with user and promo details
            console.log(`Error for user ${uid} applying promo "${promoCode}": ${error.message}`);
            throw error;
        }

        // Handle unexpected errors
        console.error(`Unexpected error applying promo code "${promoCode}" for user ${uid}:`, error);
        throw new functions.https.HttpsError(
            'internal',
            'An internal error occurred while applying the promo code.'
        );
    }
});
