const functions = require('firebase-functions');
const admin = require('firebase-admin');
// const crypto = require('crypto');

admin.initializeApp();

// exports.onGroupCreate = functions.firestore
//   .document('groups/{groupId}')
//   .onCreate(async (snap, context) => {
//     const groupId = context.params.groupId;

//     try {
//       // Define custom claims or payload as needed
//       const customClaims = {
//         groupId: groupId,
//         // Add other claims if necessary
//       };

//       // Use a unique UID for the group
//       const uid = `group_${groupId}`;

//       // Generate the custom token
//       const publicToken = await admin.auth().createCustomToken(uid, customClaims);

//       // Update the Firestore document with the custom token
//       await snap.ref.update({ publicToken });

//       console.log('Custom token generated and added to group document');
//     } catch (error) {
//       console.error('Error creating custom token:', error);
//     }
//   });

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
