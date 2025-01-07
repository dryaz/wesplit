module.exports = async (ctx) => {
  const messageText = ctx.message.text;

  // Check if the message is explicitly for this bot
  if (ctx.message.text.includes(`@${ctx.botInfo.username}`) || ctx.chat.type === "private") {
    // Extract the command arguments
    const args = messageText.split(" ").slice(1); // Exclude "/addExpense"
    const amount = parseFloat(args[0]);
    const description = args.slice(1).join(" ");

    if (isNaN(amount) || !description) {
      await ctx.reply("Invalid format. Please use: `/addExpense <amount> <description>`.");
      return;
    }

    // Process the expense
    await ctx.reply(`Expense added:\nAmount: ${amount}\nDescription: ${description}`);
  }
};
