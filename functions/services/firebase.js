const admin = require("firebase-admin");

// Initialize Firebase Admin if not already initialized
if (!admin.apps.length) {
  admin.initializeApp();
}

// Get Firestore instance from Admin SDK
const firestore = admin.firestore();

module.exports = {
  admin,
  firestore,
};
