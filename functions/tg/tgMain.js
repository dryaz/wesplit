const functions = require("firebase-functions/v2");
const express = require("express");
const bodyParser = require("body-parser");
const { Telegraf, Markup, composer, Scenes, session } = require("telegraf"); // Import Markup for inline keyboards
const { Firestore } = require("@google-cloud/firestore");

const getSecret = require("../services/secrets");

const startCommand = require("./commands/start");
const groupsCommand = require("./commands/groups");
const addExpenseCommand = require("./commands/addExpense");

const getOrCreateUserAction = require("./actions/getCreateUser");
const getLinkedGroupAction = require("./actions/getLinkedGroup");
const createGroupAction = require("./actions/createGroup");
const linkGroupAction = require("./actions/linkGroup");
const infoAction = require("./actions/info");

const inlineAction = require("./actions/inline/main");

const addExpenseScene = require("./scenes/addExpense"); // Import the scene

const stage = new Scenes.Stage([addExpenseScene]);

const firestore = new Firestore();

const app = express();
app.use(bodyParser.json());

let bot; // Placeholder for the bot instance

app.post("/", async (req, res) => {
  try {
    if (!bot) {
      const BOT_TOKEN = await getSecret("tg_bot_token"); // Fetch or use cached token
      bot = new Telegraf(BOT_TOKEN);

      // Fetch bot info during initialization
      const botInfo = await bot.telegram.getMe();
      const botUsername = botInfo.username;

      bot.start(startCommand);

      // bot.use(session());
      // bot.use(stage.middleware());

      bot.command("groups", groupsCommand);
      bot.command("linkedGroup", getLinkedGroupAction);
      bot.command("add", addExpenseCommand);
      bot.command("user", getOrCreateUserAction);

      // Inline Query Handler
      bot.on("inline_query", inlineAction);

      // Debug Action
      bot.command("info", infoAction);

      bot.action("create_new_group", createGroupAction);
      bot.action("link_existing_group", linkGroupAction);

      // bot.action("add_expense_confirm", async (ctx) => {
      //   console.log(ctx.update);
      //   try {
      //     await ctx.answerCbQuery();
      //     await ctx.telegram.sendMessage(ctx.from.id, "Entering Add Expense Scene...");
      //     ctx.scene.enter("addExpense");
      //   } catch (error) {
      //     console.error("Error entering addExpense scene:", error);
      //   }
      // });

      // const buttons = Markup.inlineKeyboard([[Markup.button.callback("Link a Wesplit Group ", "button_click")]]);

      // Handle button clicks
      // bot.action("button_click", (ctx) => {
      //   ctx.answerCbQuery(); // Acknowledge the button click
      //   // Extract the group ID (chat ID) from the context
      //   const groupId = ctx.chat?.id;

      //   // Use the group ID as needed
      //   if (groupId) {
      //     ctx.reply(`Group ID is: ${groupId} | User ID is: ${ctx.from.id}`);
      //   } else {
      //     ctx.reply("This action is not in a group context.");
      //   }
      // });

      // Handle mentions of the bot in text messages
      // bot.on("text", (ctx) => {
      //   const messageText = ctx.message.text;

      //   // Check if the bot is mentioned
      //   if (messageText.includes(`@${botUsername}`)) {
      //     ctx.reply("You mentioned me! Here’re buttons:", {
      //       reply_markup: buttons.reply_markup,
      //     });
      //   }
      // });
    }

    // Handle updates
    await bot.handleUpdate(req.body);
    res.status(200).send("OK");
  } catch (error) {
    console.error("Error handling Telegram update:", error);
    res.status(500).send("Internal Server Error");
  }
});

// Export as Firebase Function
exports.telegramBot = functions.https.onRequest(app);
