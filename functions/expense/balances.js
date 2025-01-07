const { admin, firestore } = require("../services/firebase");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");

// Cloud Function to recalculate balances and update lastExpenseAt
exports.recalculateBalancesV2 = onDocumentWritten("groups/{groupId}/expenses/{expenseId}", async (event) => {
  const groupId = event.params.groupId;
  const groupRef = firestore.collection("groups").doc(groupId);

  // Initialize balances map
  let balancesMap = {};

  // Initialize undistributed amounts map
  let undistributedMap = {};

  // Query expenses where status == 'NEW' or status == null
  const expensesQuery = groupRef.collection("expenses").where("status", "==", "NEW");

  // Fetch all expenses in the group's expenses subcollection
  const expensesSnapshot = await expensesQuery.get();

  // Iterate over each expense to update balances
  expensesSnapshot.forEach((expenseDoc) => {
    const expenseData = expenseDoc.data();
    const payedBy = expenseData.payedBy; // Participant object with 'id' and 'name'
    const amount = expenseData.totalAmount; // Amount object with 'value' and 'currencyCode'
    const shares = expenseData.shares; // Set of Share objects

    // Validate 'payedBy' and 'amount'
    if (!payedBy || !payedBy.id || !payedBy.name || !amount || typeof amount.value !== "number" || !amount.currency) {
      console.error(`Expense ${expenseDoc.id} has invalid 'payedBy' or 'amount' data.`);
      return;
    }

    const currency = amount.currency;
    const totalPaid = amount.value;

    // Initialize balances for payer if not already done
    const payerId = payedBy.id;
    if (!balancesMap[payerId]) {
      balancesMap[payerId] = {
        participant: payedBy,
        amountsMap: {},
      };
    }
    if (!balancesMap[payerId].amountsMap[currency]) {
      balancesMap[payerId].amountsMap[currency] = 0;
    }

    // Credit the payer with the total amount paid
    balancesMap[payerId].amountsMap[currency] += totalPaid;

    // Initialize sum of shares
    let sumOfShares = 0;

    // Process shares
    shares.forEach((share) => {
      const participant = share.participant; // Participant object
      const shareAmount = share.amount; // Amount object with 'value' and 'currencyCode'

      // Validate participant and shareAmount
      if (
        !participant ||
        !participant.id ||
        !participant.name ||
        !shareAmount ||
        typeof shareAmount.value !== "number" ||
        !shareAmount.currency
      ) {
        console.error(`Share in expense ${expenseDoc.id} has invalid participant or amount data.`);
        return;
      }

      if (shareAmount.currency !== currency) {
        console.error(`Currency mismatch in expense ${expenseDoc.id}.`);
        return;
      }

      const participantId = participant.id;
      const shareValue = shareAmount.value;

      // Initialize balances for participant if not already done
      if (!balancesMap[participantId]) {
        balancesMap[participantId] = {
          participant: participant,
          amountsMap: {},
        };
      }
      if (!balancesMap[participantId].amountsMap[currency]) {
        balancesMap[participantId].amountsMap[currency] = 0;
      }

      // Debit the participant by their share amount
      balancesMap[participantId].amountsMap[currency] -= shareValue;

      // Accumulate the sum of shares
      sumOfShares += shareValue;
    });

    // Calculate undistributed amount
    const undistributedAmount = totalPaid - sumOfShares;

    const EPSILON = 1e-5;

    if (Math.abs(undistributedAmount) > EPSILON) {
      // Initialize undistributed amount for this currency if not already done
      if (!undistributedMap[currency]) {
        undistributedMap[currency] = 0;
      }
      // Add undistributed amount to the total for this currency
      undistributedMap[currency] += undistributedAmount;
    }
  });

  // Construct participantsBalance set
  const participantsBalanceSet = Object.values(balancesMap).map((entry) => {
    // Convert amountsMap to a set of Amount objects
    const amountsSet = Object.entries(entry.amountsMap).map(([currency, value]) => ({
      currency,
      value,
    }));

    return {
      participant: entry.participant,
      amounts: amountsSet,
    };
  });

  // Construct undistributed set
  const undistributedSet = Object.entries(undistributedMap).map(([currency, value]) => ({
    currency,
    value,
  }));

  // Create the Balance object
  const balanceObject = {
    participantsBalance: participantsBalanceSet,
    undistributed: undistributedSet,
  };

  // Prepare the update data
  const updateData = {
    balances: balanceObject, // Set containing one Balance object
    lastExpenseAt: admin.firestore.FieldValue.serverTimestamp(),
  };

  // Update the group document with the recalculated balances and lastExpenseAt
  await groupRef.update(updateData);

  // After recalculating balances, send notifications for new expense
  //    if (!change.before.exists) { // Only send if it's a new expense (not an update or deletion)
  //      const expense = change.after.data();
  //      console.log(`Send notifications for new expense`);
  //      await sendNotifications(expense, groupId);
  //    }
});
