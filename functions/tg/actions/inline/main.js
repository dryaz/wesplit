const { Markup } = require("telegraf");

module.exports = (ctx) => {
  const query = ctx.inlineQuery.query;

  // Define inline results (buttons)
  const results = [
    {
      type: "article",
      id: "1",
      title: query,
      input_message_content: {
        message_text: "You clicked the inline button!",
      },
      reply_markup: Markup.inlineKeyboard([
        [
          Markup.button.switchToCurrentChat(
            "Add Expense", // Button text
            "/add@WeSplitAppBot " // Text pre-filled in the input box,
          ),
        ],
      ]).reply_markup,
      description: "Connect to Wesplit to manager expenses",
    },
    {
      type: "article",
      id: "2",
      title: "Open Wesplit app",
      input_message_content: {
        message_text: "Check this awesome group details in Wepslit",
      },
      reply_markup: Markup.inlineKeyboard([[Markup.button.url("Open Wesplit", "https://web.wesplit.app")]]).reply_markup,
      description: "See detailed info about group",
    },
  ];

  ctx.answerInlineQuery(results);
};
