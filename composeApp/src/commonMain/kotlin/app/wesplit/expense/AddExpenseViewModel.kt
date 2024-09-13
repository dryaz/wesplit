package app.wesplit.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.expense.ExpenseType
import app.wesplit.domain.model.expense.Share
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.Participant
import app.wesplit.routing.RightPane
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent

class AddExpenseViewModel(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
) : ViewModel(), KoinComponent {
    // TODO: savedStateHandle should be used to support add expense inside group
    private val groupId: String =
        checkNotNull(
            savedStateHandle[
                RightPane
                    .AddExpense
                    .Param
                    .GROUP_ID
                    .paramName,
            ],
        )

    // TODO: savedStateHandle should be used to support editing expense
    val expenseId: String? =
        savedStateHandle[
            RightPane
                .AddExpense
                .Param
                .EXPENSE_ID
                .paramName,
        ]

    val state: StateFlow<State>
        get() = _state

    private val _state = MutableStateFlow<State>(State.Loading)

    init {
        _state.update {
            State.Data(
                group =
                    Group(
                        id = "123",
                        title = "Awesome Group",
                        participants =
                            setOf(
                                Participant("123", "Dmitrii", isMe = true),
                                Participant("124", "Ivan"),
                                Participant("125", "Marko"),
                                Participant("126", "Tanya"),
                            ),
                    ),
                expense =
                    Expense(
                        id = null,
                        title = "Some expense",
                        payedBy = Participant("12#", "Dmitrii"),
                        shares =
                            listOf(
                                Share(
                                    participant = Participant("123", "Dmitrii", isMe = true),
                                    amount = Amount(25f, "USD"),
                                ),
                                Share(
                                    participant = Participant("124", "Ivan"),
                                    amount = Amount(25f, "USD"),
                                ),
                                Share(
                                    participant = Participant("126", "Tanya"),
                                    amount = Amount(25f, "USD"),
                                ),
                            ),
                        totalAmount =
                            Amount(
                                value = 75f,
                                currencyCode = "USD",
                            ),
                        ExpenseType.EXPENSE,
                        date = Clock.System.now(),
                    ),
            )
        }
    }

    sealed interface State {
        data object Loading : State

        data class Error(val type: Type) : State {
            enum class Type {
                NOT_EXISTS,
                UNAUTHORIZED,
                FETCH_ERROR,
            }
        }

        data class Data(
            val group: Group,
            val expense: Expense,
        ) : State
    }
}
