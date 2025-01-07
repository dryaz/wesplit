const { firestore } = require("../../services/firebase");
const { Markup } = require("telegraf");

const groupCache = new Map(); // In-memory cache for groups

module.exports = async (ctx) => {
  const telegramChatId = ctx.chat.id.toString(); // Current chat ID

  // Check cache first
  if (groupCache.has(telegramChatId)) {
    console.log("Returning group from cache");
    const cachedGroup = groupCache.get(telegramChatId);
    return cachedGroup;
  }

  try {
    const groupsCollection = firestore.collection("groups");

    // Query Firestore for a group with tgGroup equal to the current chat ID
    const snapshot = await groupsCollection.where("tgGroup", "==", telegramChatId).get();

    if (!snapshot.empty) {
      // Group found, return the group data
      const groupDoc = snapshot.docs[0];
      const group = groupDoc.data();

      // Cache the group data
      groupCache.set(telegramChatId, group);
      console.log("Group cached:", group);

      return group;
    } else {
      // No group found, respond with buttons
      await ctx.reply("There is no linked Wesplit group to this chat.", {
        reply_markup: Markup.inlineKeyboard([
          [
            Markup.button.callback("Link existing Group", "link_existing_group"),
            Markup.button.callback("Create new Group", "create_new_group"),
          ],
        ]).reply_markup,
      });
      return null;
    }
  } catch (error) {
    console.error("Error fetching group by chat ID:", error);
    await ctx.reply("An error occurred while processing your request.");
  }
};
