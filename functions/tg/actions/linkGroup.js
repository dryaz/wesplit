module.exports = async (ctx) => {
  const telegramChatId =
    ctx.chat?.id || // Direct chat context
    ctx.callbackQuery?.message?.chat?.id || // Callback query context
    null;

  await ctx.telegram.sendMessage(telegramChatId, `TODO: Link new Group to ${telegramChatId}`);
};
