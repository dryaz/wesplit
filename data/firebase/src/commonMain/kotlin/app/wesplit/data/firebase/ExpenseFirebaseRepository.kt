package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.expense.ExpenseType
import app.wesplit.domain.model.expense.Share
import app.wesplit.domain.model.group.Participant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

private const val GET_EXPENSE_EVENT = "expenses_get"
private const val GET_EXPENSE_SIZE_PARAM = "size"

@Single
class ExpenseFirebaseRepository(
    private val analyticsManager: AnalyticsManager,
) : ExpenseRepository {
    // TODO: Check if firebase get local balances or maybe need to cache expenses by group id in order
    //  not to fetch this multiple times, e.g. for showing trxs and for computing balances.
    override fun getByGroupId(groupId: String): Flow<List<Expense>> =
        flow {
            val expenses = fakeData()
            analyticsManager.track(GET_EXPENSE_EVENT, mapOf(GET_EXPENSE_SIZE_PARAM to "${expenses.size}"))
            emit(expenses)
        }
}

fun fakeData() =
    (0..30).map {
        var total = Random.nextInt(1, 10000)
        val first = Random.nextInt(1, total)
        val second = Random.nextInt(1, total - first)
        val third = total - first - second - 1
        Expense(
            id = "$it",
            title = "Expense #$it",
            shares =
                listOf(
                    Share(
                        participant = Participant(name = "User 1", isMe = Random.nextFloat() > 0.5f),
                        amount =
                            Amount(
                                value = first.toFloat() / 100f,
                                currencyCode = "USD",
                            ),
                    ),
                    Share(
                        participant = Participant(name = "User 2"),
                        amount =
                            Amount(
                                value = second.toFloat() / 100f,
                                currencyCode = "USD",
                            ),
                    ),
                    Share(
                        participant = Participant(name = "User 3"),
                        amount =
                            Amount(
                                value = third.toFloat() / 100f,
                                currencyCode = "USD",
                            ),
                    ),
                ),
            totalAmount =
                Amount(
                    value = total.toFloat() / 100f,
                    currencyCode = "USD",
                ),
            type = ExpenseType.EXPENSE,
            date = Clock.System.now().plus((12 * it).days),
            payedBy = Participant(name = "User 1", isMe = Random.nextFloat() > 0.5f),
        )
    }
