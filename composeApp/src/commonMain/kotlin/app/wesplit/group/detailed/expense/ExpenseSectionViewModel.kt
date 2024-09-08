package app.wesplit.group.detailed.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent

private const val PAGE_SIZE = 30

class ExpenseSectionViewModel(
    private val groupId: String,
    private val expenseRepository: ExpenseRepository,
) : ViewModel(),
    KoinComponent {
    val dataState: StateFlow<State>
        get() = _dataState

    private val _dataState = MutableStateFlow<State>(State.Loading)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            expenseRepository.getByGroupId(groupId).collectLatest { expenses ->
                _dataState.update {
                    State.Expenses(
                        expenses.groupBy {
                            val localDate = it.date.toLocalDateTime(TimeZone.currentSystemDefault())
                            "${localDate.month} ${localDate.year}"
                        },
                    )
                }
            }
        }
    }

    fun loadNextPage() {
        TODO("Implement")
    }

    sealed interface State {
        data object Loading : State

        data object Empty : State

        data object Unauthorized : State

        data class Expenses(
            val groupedExpenses: Map<String, List<Expense>>,
        ) : State
    }
}
