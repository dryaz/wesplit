module.exports = {
  testEnvironment: "node",
  verbose: true, // Show detailed test results
  setupFilesAfterEnv: ["./jest.setup.js"], // Optional setup file
  testTimeout: 30000, // Increase timeout for Firebase emulation
};
