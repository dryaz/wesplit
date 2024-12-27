const { firestore } = require("../../services/firebase");

module.exports = async (ctx) => {
  try {
    // Get the Telegram user ID from the context
    const telegramUserId = ctx.from.id.toString(); // Convert to string if needed

    // Query Firestore for groups where tokens array contains the Telegram user ID
    const snapshot = await firestore.collection("groups").where("tokens", "array-contains", telegramUserId).get();

    if (snapshot.empty) {
      await ctx.reply("You are not a part of any groups in WeSplit.");
      return;
    }

    // Extract group titles
    let groupTitles = [];
    snapshot.forEach((doc) => {
      const data = doc.data();
      if (data.title) {
        groupTitles.push(data.title);
      }
    });

    // Send the list of group titles back to the user
    const replyText = groupTitles.length
      ? `Here're your groups :\n\n${groupTitles.map((title, index) => `${index + 1}. ${title}`).join("\n")}`
      : "No groups found.";

    await ctx.reply(replyText);
  } catch (error) {
    console.error("Error fetching groups:", error);
    await ctx.reply("An error occurred while fetching your groups. Please try again later.");
  }
};
