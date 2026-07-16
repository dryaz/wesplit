// TODO: Export functions one by one from legacy when work on adjustments
const { telegramBot } = require("./tg/tgMain");
const { recalculateBalancesV2 } = require("./expense/balances");
const { notificationOnNewExpenseV2 } = require("./expense/notification");
const { handleImageGeneration } = require("./group/imageGeneration");

const {
  assignCategoryOnExpenseWrite,
  generateGroupToken,
  updateCurrencyRates,
  updateExpensesStatus,
  handleSubscriptionUpdates,
  handleAppleServerNotificationDev,
  handleAppleServerNotification,
  updateUserSubscription,
  applyPromoCode,
} = require("./legacy");

module.exports = {
  telegramBot,
  recalculateBalancesV2,
  notificationOnNewExpenseV2,
  handleImageGeneration,
  assignCategoryOnExpenseWrite,
  generateGroupToken,
  updateCurrencyRates,
  updateExpensesStatus,
  handleSubscriptionUpdates,
  handleAppleServerNotificationDev,
  handleAppleServerNotification,
  updateUserSubscription,
  applyPromoCode,
};
