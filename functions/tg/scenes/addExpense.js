const { Scenes } = require("telegraf");

const addExpenseScene = new Scenes.BaseScene("addExpense");

// Step 1: Enter the scene
addExpenseScene.enter();

// Step 2: Handle user input
addExpenseScene.on("text", async (ctx) => {
  const message = ctx.message.text;

  const parts = message.split(" ");
  const amount = parseFloat(parts[0]);
  const description = parts.slice(1).join(" ");

  if (isNaN(amount) || !description) {
    await ctx.reply("Invalid format. Please use `<amount> <description>`.");
    return;
  }

  // Process the expense (e.g., save to database)
  await ctx.reply(`Expense added:\nAmount: ${amount}\nDescription: ${description}`);

  // Exit the scene
  return ctx.scene.leave();
});

// Step 3: Handle exit
addExpenseScene.leave((ctx) => ctx.reply("Thank you!"));

module.exports = addExpenseScene;
