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

@Single
class ExpenseFirebaseRepository(
    private val analyticsManager: AnalyticsManager,
) : ExpenseRepository {
    override fun getByGroupId(
        groupId: String,
        cursorExpenseId: String?,
    ): Flow<List<Expense>> =
        flow {
            emit(fakeData())
        }
}

fun fakeData() =
    (0..30).map {
        var total = Random.nextInt(1, 10000)
        val first = Random.nextInt(1, total)
        val second = Random.nextInt(1, total - first)
        val third = total - first - second
        Expense(
            id = "$it",
            title = "Expense #$it",
            shares =
                listOf(
                    Share(
                        participant = Participant(name = "User 1", isMe = Random.nextFloat() > 0.5f),
                        amount =
                            Amount(
                                amount = first.toFloat() / 100f,
                                currencyCode = "USD",
                            ),
                    ),
                    Share(
                        participant = Participant(name = "User 2"),
                        amount =
                            Amount(
                                amount = second.toFloat() / 100f,
                                currencyCode = "USD",
                            ),
                    ),
                    Share(
                        participant = Participant(name = "User 3"),
                        amount =
                            Amount(
                                amount = third.toFloat() / 100f,
                                currencyCode = "USD",
                            ),
                    ),
                ),
            totalAmount =
                Amount(
                    amount = total.toFloat() / 100f,
                    currencyCode = "USD",
                ),
            type = ExpenseType.EXPENSE,
            date = Clock.System.now().plus((12 * it).days),
            payedBy = Participant(name = "User 1", isMe = Random.nextFloat() > 0.5f),
        )
    }
