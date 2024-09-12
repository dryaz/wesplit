package app.wesplit.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.routing.RightPane
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
            State.Expense(
                id = null,
                title = null,
                total = Amount(0f, "USD"),
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

        // TODO: We should have expense on domain layer
        data class Expense(
            val id: String?,
            val title: String?,
            val total: Amount,
        ) : State
    }
}
