const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.onGroupCreate = functions.firestore
  .document('groups/{groupId}')
  .onCreate(async (snap, context) => {
    const groupId = context.params.groupId;

    try {
      // Define custom claims or payload as needed
      const customClaims = {
        groupId: groupId,
        // Add other claims if necessary
      };

      // Use a unique UID for the group
      const uid = `group_${groupId}`;

      // Generate the custom token
      const publicToken = await admin.auth().createCustomToken(uid, customClaims);

      // Update the Firestore document with the custom token
      await snap.ref.update({ publicToken });

      console.log('Custom token generated and added to group document');
    } catch (error) {
      console.error('Error creating custom token:', error);
    }
  });
