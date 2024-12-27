module.exports = async (ctx) => {
  const telegramChatId = ctx.chat.id.toString(); // Current chat ID

  await ctx.reply(`TODO: Create new Group`);
};
