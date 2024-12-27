module.exports = async (ctx) => {
  const telegramUserId = ctx.from.id.toString();
  const telegramChatId = ctx.chat.id.toString(); // Current chat ID

  await ctx.reply(`ChatID: ${telegramChatId}\nUserID: ${telegramUserId}`);
};
