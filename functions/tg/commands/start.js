const { firestore } = require("../../services/firebase");

module.exports = async (ctx) => {
  try {
    const telegramUserId = ctx.from.id.toString();
    const telegramUserName = ctx.from.first_name;

    // Use the Firestore service to query the "users" collection
    const snapshot = await firestore.collection("users").where("authIds", "array-contains", telegramUserId).get();

    if (!snapshot.empty) {
      const userDoc = snapshot.docs[0];
      const userName = userDoc.data().name;
      await ctx.reply(`Hello, ${userName}`);
    } else {
      await ctx.reply(
        `Hello, ${telegramUserName}, you have not linked your Telegram account to WeSplit. You could continue right away, and we'll create an account for you. Or you could link your Telegram account from your profile on https://web.wesplit.app.`
      );
    }
  } catch (error) {
    console.error("Error in /start command:", error);
    await ctx.reply("An error occurred while processing your request.");
  }
};
