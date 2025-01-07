// TODO: Export functions one by one from legacy when work on adjustments
const { telegramBot } = require("./tg/tgMain");
const { recalculateBalancesV2 } = require("./expense/balances");

module.exports = {
  telegramBot,
  recalculateBalancesV2,
};
