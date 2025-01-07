const { SecretManagerServiceClient } = require("@google-cloud/secret-manager");

// Initialize the Secret Manager client
const secretClient = new SecretManagerServiceClient();

const cachedSecrets = {}; // In-memory cache for secrets

/**
 * Fetches a secret from Google Cloud Secret Manager by its name.
 * Uses in-memory caching to avoid redundant API calls.
 *
 * @param {string} secretName - The name of the secret in the format "projects/<project-id>/secrets/<secret-id>/versions/<version>"
 * @returns {Promise<string>} The secret value as a string.
 */
async function getSecret(secretName) {
  if (cachedSecrets[secretName]) {
    return cachedSecrets[secretName]; // Return cached secret if available
  }

  console.log(`Fetching secret from Secret Manager: ${secretName}`);
  try {
    const [accessResponse] = await secretClient.accessSecretVersion({
      name: `projects/548791587175/secrets/${secretName}/versions/latest`,
    });

    const secretValue = accessResponse.payload.data.toString("utf8");
    cachedSecrets[secretName] = secretValue; // Cache the secret value
    return secretValue;
  } catch (error) {
    console.error(`Error fetching secret '${secretName}':`, error);
    throw new Error(`Unable to fetch secret: ${secretName}`);
  }
}

module.exports = getSecret;
