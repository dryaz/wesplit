const { Markup } = require("telegraf");
const getLinkedGroup = require("../getLinkedGroup"); // Adjust the path to your getLinkedGroup module

module.exports = async (ctx) => {
  try {
    const query = ctx.inlineQuery.query.trim(); // User's inline query
    const telegramChatId = ctx.chat?.id?.toString(); // Current chat ID (if available)

    const results = []; // Inline query results

    if (!query) {
      results.push({
        type: "article",
        id: "1",
        title: "Show what I could do",
        description: "Press to link or create a group.",
        input_message_content: {
          message_text: "No group is linked to this chat. Please link or create a new group.",
        },
        reply_markup: Markup.inlineKeyboard([
          [Markup.button.callback("Link Existing Group", "link_existing_group")],
          [Markup.button.callback("Create New Group", "create_new_group")],
        ]).reply_markup,
      });
    } else {
      // Case 2: Query is not empty
      results.push({
        type: "article",
        id: "3",
        title: `$${query}`,
        description: "Add expense by natural language",
        input_message_content: {
          message_text: `Expense added: ${query}`,
        },
      });
    }

    // Return the inline query results
    await ctx.answerInlineQuery(results, { cache_time: 0 });
  } catch (error) {
    console.error("Error handling inline_query:", error);
  }
};
