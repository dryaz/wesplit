const { firestore } = require("../../services/firebase");

const userCache = new Map(); // In-memory cache for user data

module.exports = async (ctx) => {
  const telegramUserId = ctx.from.id.toString();

  // Check cache first
  if (userCache.has(telegramUserId)) {
    console.log("Returning user from cache");
    const user = userCache.get(telegramUserId);
    await ctx.reply(`Welcome back, ${user.name}!`);
    return user;
  }

  try {
    // Retrieve or create user as before
    const telegramUsername = ctx.from.username ? `@${ctx.from.username}` : null;
    const telegramName = ctx.from.first_name || ctx.from.last_name || "Unknown";
    const userPhotoUrl = ctx.from.photo_url || null;

    const usersCollection = firestore.collection("users");
    const snapshot = await usersCollection.where("authIds", "array-contains", telegramUserId).get();

    let user;
    if (!snapshot.empty) {
      const userDoc = snapshot.docs[0];
      user = userDoc.data();
    } else {
      user = {
        name: telegramName,
        photoUrl: userPhotoUrl,
        contacts: telegramUsername ? [{ type: "telegram", account: telegramUsername }] : [],
        authIds: [telegramUserId],
      };
      const newUserRef = await usersCollection.add(user);
      user.id = newUserRef.id;
    }

    // Cache the user data
    userCache.set(telegramUserId, user);
    console.log("User cached:", user);

    await ctx.reply(`Welcome, ${user.name}!`);
    return user;
  } catch (error) {
    console.error("Error in getOrCreateUser:", error);
    await ctx.reply("An error occurred while processing your request.");
  }
};
