// balances.test.js
const firebaseFunctionsTest = require("firebase-functions-test")({
  projectId: "test-project",
});
const { admin, firestore } = require("../../services/firebase");
const myFunctions = require("../balances"); // Adjust the path as needed
const { v4: uuidv4 } = require("uuid");

// Helper function to compare balances regardless of order.
function compareBalances(actual, expected) {
  // Compare participantsBalance.
  expected.participantsBalance.forEach(expectedParticipant => {
    const actualParticipant = actual.participantsBalance.find(
      p => p.participant.id === expectedParticipant.participant.id
    );
    expect(actualParticipant).toBeDefined();
    expectedParticipant.amounts.forEach(expectedAmount => {
      const actualAmount = actualParticipant.amounts.find(
        a => a.currency === expectedAmount.currency
      );
      expect(actualAmount).toBeDefined();
      expect(actualAmount.value).toEqual(expectedAmount.value);
    });
  });

  // Compare undistributed amounts.
  expected.undistributed.forEach(expectedUndist => {
    const actualUndist = actual.undistributed.find(
      u => u.currency === expectedUndist.currency
    );
    expect(actualUndist).toBeDefined();
    expect(actualUndist.value).toEqual(expectedUndist.value);
  });
}

describe("Integration tests for recalculateBalancesV2", () => {
  let wrappedFunction;

  beforeAll(() => {
    // Wrap your cloud function to simulate the Firestore onDocumentWritten event.
    wrappedFunction = firebaseFunctionsTest.wrap(myFunctions.recalculateBalancesV2);
  });

  afterAll(async () => {
    await firebaseFunctionsTest.cleanup();
  });

  // Clean up test data after each test.
  afterEach(async () => {
    const groupsSnapshot = await firestore.collection("groups").get();
    const deletes = [];
    groupsSnapshot.forEach(doc => deletes.push(doc.ref.delete()));
    await Promise.all(deletes);
  });

  it("should calculate balances for a valid expense with no undistributed amount", async () => {
    // Setup: Expense total 100 with shares 50 and 50 => undistributed 0.
    const groupId = uuidv4();
    const groupRef = firestore.collection("groups").doc(groupId);
    await groupRef.set({});

    const expenseRef = groupRef.collection("expenses").doc("expense1");
    await expenseRef.set({
      payedBy: { id: "user1", name: "User One" },
      totalAmount: { value: 100, currency: "USD" },
      shares: [
        { participant: { id: "user1", name: "User One" }, amount: { value: 50, currency: "USD" } },
        { participant: { id: "user2", name: "User Two" }, amount: { value: 50, currency: "USD" } },
      ],
      status: "NEW",
    });

    const fakeEvent = { params: { groupId, expenseId: "expense1" } };
    await wrappedFunction(fakeEvent);

    const updatedGroup = (await groupRef.get()).data();
    const expectedBalances = {
      participantsBalance: [
        { participant: { id: "user1", name: "User One" }, amounts: [{ currency: "USD", value: 50 }] },
        { participant: { id: "user2", name: "User Two" }, amounts: [{ currency: "USD", value: -50 }] },
      ],
      undistributed: [],
    };

    compareBalances(updatedGroup.balances, expectedBalances);
    expect(updatedGroup.lastExpenseAt).toBeDefined();
  });

  it("should calculate balances with a positive undistributed amount", async () => {
    // Setup: Expense total 100 with shares 30 and 40 => undistributed = 30.
    const groupId = uuidv4();
    const groupRef = firestore.collection("groups").doc(groupId);
    await groupRef.set({});

    const expenseRef = groupRef.collection("expenses").doc("expense2");
    await expenseRef.set({
      payedBy: { id: "user1", name: "User One" },
      totalAmount: { value: 100, currency: "USD" },
      shares: [
        { participant: { id: "user1", name: "User One" }, amount: { value: 30, currency: "USD" } },
        { participant: { id: "user2", name: "User Two" }, amount: { value: 40, currency: "USD" } },
      ],
      status: "NEW",
    });

    const fakeEvent = { params: { groupId, expenseId: "expense2" } };
    await wrappedFunction(fakeEvent);

    const updatedGroup = (await groupRef.get()).data();
    // Calculation: user1: +100 - 30 = +70, user2: -40, undistributed: 100 - (30+40) = 30.
    const expectedBalances = {
      participantsBalance: [
        { participant: { id: "user1", name: "User One" }, amounts: [{ currency: "USD", value: 70 }] },
        { participant: { id: "user2", name: "User Two" }, amounts: [{ currency: "USD", value: -40 }] },
      ],
      undistributed: [{ currency: "USD", value: 30 }],
    };

    compareBalances(updatedGroup.balances, expectedBalances);
    expect(updatedGroup.lastExpenseAt).toBeDefined();
  });

  it("should calculate balances with a negative undistributed amount", async () => {
    // Setup: Expense total 100 with shares 80 and 40 => undistributed = -20.
    const groupId = uuidv4();
    const groupRef = firestore.collection("groups").doc(groupId);
    await groupRef.set({});

    const expenseRef = groupRef.collection("expenses").doc("expense3");
    await expenseRef.set({
      payedBy: { id: "user1", name: "User One" },
      totalAmount: { value: 100, currency: "USD" },
      shares: [
        { participant: { id: "user1", name: "User One" }, amount: { value: 80, currency: "USD" } },
        { participant: { id: "user2", name: "User Two" }, amount: { value: 40, currency: "USD" } },
      ],
      status: "NEW",
    });

    const fakeEvent = { params: { groupId, expenseId: "expense3" } };
    await wrappedFunction(fakeEvent);

    const updatedGroup = (await groupRef.get()).data();
    // Calculation: user1: +100 - 80 = +20, user2: -40, undistributed: 100 - (80+40) = -20.
    const expectedBalances = {
      participantsBalance: [
        { participant: { id: "user1", name: "User One" }, amounts: [{ currency: "USD", value: 20 }] },
        { participant: { id: "user2", name: "User Two" }, amounts: [{ currency: "USD", value: -40 }] },
      ],
      undistributed: [{ currency: "USD", value: -20 }],
    };

    compareBalances(updatedGroup.balances, expectedBalances);
    expect(updatedGroup.lastExpenseAt).toBeDefined();
  });

  it("should aggregate balances correctly over multiple expenses", async () => {
    // Create a group and add two separate expenses.
    const groupId = uuidv4();
    const groupRef = firestore.collection("groups").doc(groupId);
    await groupRef.set({});

    // Expense 1: Valid expense with no undistributed.
    const expenseRef1 = groupRef.collection("expenses").doc("expense1");
    await expenseRef1.set({
      payedBy: { id: "user1", name: "User One" },
      totalAmount: { value: 100, currency: "USD" },
      shares: [
        { participant: { id: "user1", name: "User One" }, amount: { value: 50, currency: "USD" } },
        { participant: { id: "user2", name: "User Two" }, amount: { value: 50, currency: "USD" } },
      ],
      status: "NEW",
    });

    // Expense 2: Valid expense with negative undistributed.
    const expenseRef2 = groupRef.collection("expenses").doc("expense2");
    await expenseRef2.set({
      payedBy: { id: "user2", name: "User Two" },
      totalAmount: { value: 200, currency: "USD" },
      shares: [
        { participant: { id: "user1", name: "User One" }, amount: { value: 60, currency: "USD" } },
        { participant: { id: "user2", name: "User Two" }, amount: { value: 100, currency: "USD" } },
        { participant: { id: "user3", name: "User Three" }, amount: { value: 50, currency: "USD" } },
      ],
      status: "NEW",
    });

    // Trigger the function (using any expense event is fine as the function queries all NEW expenses)
    const fakeEvent = { params: { groupId, expenseId: "expense2" } };
    await wrappedFunction(fakeEvent);

    const updatedGroup = (await groupRef.get()).data();
    // Expense 1: user1: +100 - 50 = +50, user2: -50, undistributed: 0.
    // Expense 2: user2: +200 - 100 = +100, user1: -60, user3: -50, undistributed: 200 - (60+100+50) = -10.
    // Totals: user1: 50 - 60 = -10, user2: -50 + 100 = +50, user3: -50; overall undistributed: -10.
    const expectedBalances = {
      participantsBalance: [
        { participant: { id: "user1", name: "User One" }, amounts: [{ currency: "USD", value: -10 }] },
        { participant: { id: "user2", name: "User Two" }, amounts: [{ currency: "USD", value: 50 }] },
        { participant: { id: "user3", name: "User Three" }, amounts: [{ currency: "USD", value: -50 }] },
      ],
      undistributed: [{ currency: "USD", value: -10 }],
    };

    compareBalances(updatedGroup.balances, expectedBalances);
    expect(updatedGroup.lastExpenseAt).toBeDefined();
  });

  it("should handle expenses in multiple currencies", async () => {
    // Create a group and add expenses in USD and EUR.
    const groupId = uuidv4();
    const groupRef = firestore.collection("groups").doc(groupId);
    await groupRef.set({});

    // USD expense.
    const expenseUSD = groupRef.collection("expenses").doc("expenseUSD");
    await expenseUSD.set({
      payedBy: { id: "user1", name: "User One" },
      totalAmount: { value: 100, currency: "USD" },
      shares: [
        { participant: { id: "user1", name: "User One" }, amount: { value: 60, currency: "USD" } },
        { participant: { id: "user2", name: "User Two" }, amount: { value: 30, currency: "USD" } },
      ],
      status: "NEW",
    });

    // EUR expense.
    const expenseEUR = groupRef.collection("expenses").doc("expenseEUR");
    await expenseEUR.set({
      payedBy: { id: "user3", name: "User Three" },
      totalAmount: { value: 150, currency: "EUR" },
      shares: [
        { participant: { id: "user3", name: "User Three" }, amount: { value: 100, currency: "EUR" } },
        { participant: { id: "user4", name: "User Four" }, amount: { value: 20, currency: "EUR" } },
      ],
      status: "NEW",
    });

    const fakeEvent = { params: { groupId, expenseId: "expenseEUR" } };
    await wrappedFunction(fakeEvent);

    const updatedGroup = (await groupRef.get()).data();
    // USD expense: user1: +100 - 60 = 40, user2: -30, undistributed: 10.
    // EUR expense: user3: +150 - 100 = 50, user4: -20, undistributed: 30.
    const expectedBalances = {
      participantsBalance: [
        { participant: { id: "user1", name: "User One" }, amounts: [{ currency: "USD", value: 40 }] },
        { participant: { id: "user2", name: "User Two" }, amounts: [{ currency: "USD", value: -30 }] },
        { participant: { id: "user3", name: "User Three" }, amounts: [{ currency: "EUR", value: 50 }] },
        { participant: { id: "user4", name: "User Four" }, amounts: [{ currency: "EUR", value: -20 }] },
      ],
      undistributed: [
        { currency: "USD", value: 10 },
        { currency: "EUR", value: 30 },
      ],
    };

    compareBalances(updatedGroup.balances, expectedBalances);
    expect(updatedGroup.lastExpenseAt).toBeDefined();
  });

  it("should skip expenses with invalid data", async () => {
    // Expense with invalid data (missing payedBy) should be skipped.
    const groupId = uuidv4();
    const groupRef = firestore.collection("groups").doc(groupId);
    await groupRef.set({});

    // Valid expense.
    const validExpense = groupRef.collection("expenses").doc("validExpense");
    await validExpense.set({
      payedBy: { id: "user1", name: "User One" },
      totalAmount: { value: 100, currency: "USD" },
      shares: [
        { participant: { id: "user1", name: "User One" }, amount: { value: 50, currency: "USD" } },
        { participant: { id: "user2", name: "User Two" }, amount: { value: 50, currency: "USD" } },
      ],
      status: "NEW",
    });

    // Invalid expense: missing payedBy.
    const invalidExpense = groupRef.collection("expenses").doc("invalidExpense");
    await invalidExpense.set({
      totalAmount: { value: 200, currency: "USD" },
      shares: [
        { participant: { id: "user1", name: "User One" }, amount: { value: 100, currency: "USD" } },
        { participant: { id: "user2", name: "User Two" }, amount: { value: 100, currency: "USD" } },
      ],
      status: "NEW",
    });

    const fakeEvent = { params: { groupId, expenseId: "invalidExpense" } };
    await wrappedFunction(fakeEvent);

    const updatedGroup = (await groupRef.get()).data();
    // Only the valid expense should be counted.
    const expectedBalances = {
      participantsBalance: [
        { participant: { id: "user1", name: "User One" }, amounts: [{ currency: "USD", value: 50 }] },
        { participant: { id: "user2", name: "User Two" }, amounts: [{ currency: "USD", value: -50 }] },
      ],
      undistributed: [],
    };

    compareBalances(updatedGroup.balances, expectedBalances);
    expect(updatedGroup.lastExpenseAt).toBeDefined();
  });

  it("should handle expense with currency mismatch by skipping invalid share", async () => {
    // In this test, an expense contains one share with a different currency.
    const groupId = uuidv4();
    const groupRef = firestore.collection("groups").doc(groupId);
    await groupRef.set({});

    const expenseRef = groupRef.collection("expenses").doc("expenseMismatch");
    await expenseRef.set({
      payedBy: { id: "user1", name: "User One" },
      totalAmount: { value: 100, currency: "USD" },
      shares: [
        // Valid share.
        { participant: { id: "user1", name: "User One" }, amount: { value: 60, currency: "USD" } },
        // Invalid share: currency mismatch.
        { participant: { id: "user2", name: "User Two" }, amount: { value: 30, currency: "EUR" } },
      ],
      status: "NEW",
    });

    // With the invalid share skipped, sum of shares = 60, undistributed = 40.
    const fakeEvent = { params: { groupId, expenseId: "expenseMismatch" } };
    await wrappedFunction(fakeEvent);

    const updatedGroup = (await groupRef.get()).data();
    // user1: credited with 100, debited 60 = +40. user2: no valid share applied.
    const expectedBalances = {
      participantsBalance: [
        { participant: { id: "user1", name: "User One" }, amounts: [{ currency: "USD", value: 40 }] },
      ],
      undistributed: [{ currency: "USD", value: 40 }],
    };

    compareBalances(updatedGroup.balances, expectedBalances);
    expect(updatedGroup.lastExpenseAt).toBeDefined();
  });
});
