const { admin, firestore } = require("../services/firebase");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");

// Dedicated function to send notifications when a new expense is created
exports.notificationOnNewExpenseV2 = onDocumentCreated("groups/{groupId}/expenses/{expenseId}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) {
    console.log("No data associated with the event");
    return;
  }

  const expense = snapshot.data();
  const groupId = event.params.groupId;

  if (!expense || !groupId) {
    console.error("Expense data or Group ID is missing");
    return;
  }

  // Check if expense.totalAmount and its fields are properly structured
  if (!expense.totalAmount || !expense.totalAmount.currency || !expense.totalAmount.value) {
    console.error("Expense totalAmount data is missing or invalid");
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

    // Check if share amount is 0, skip sending notification for this participant
    if (!share.amount || share.amount.value === 0) {
      console.warn(`Skipping notification for participant ${participant.id} as share amount is 0`);
      continue;
    }

    // Get the first authId
    const userId = participant.user.authIds[0];
    const userRef = firestore.collection("users").doc(userId);
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
