const { Firestore } = require("@google-cloud/firestore");

// Create a single Firestore instance
const firestore = new Firestore();

// Export both Firestore and Auth
module.exports = {
  firestore,
};
