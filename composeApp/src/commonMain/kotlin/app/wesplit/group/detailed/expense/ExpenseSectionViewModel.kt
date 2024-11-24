package app.wesplit.group.detailed.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.FutureFeature
import app.wesplit.domain.model.LogLevel
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.expense.toInstant
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.user.UserRepository
import app.wesplit.domain.model.user.isPlus
import app.wesplit.ui.Banner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent

private const val PAGE_SIZE = 30

class ExpenseSectionViewModel(
    private val groupId: String,
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
    private val analyticsManager: AnalyticsManager,
    private val userRepository: UserRepository,
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
            val group =
                groupRepository
                    .get(groupId)
                    .first()
                    .getOrNull()

            if (group == null) return@launch

            val expensesFlow =
                expenseRepository.getByGroupId(groupId)
                    .catch {
                        analyticsManager.log("ExpenseSectionViewModel - refresh()", LogLevel.WARNING)
                        analyticsManager.log(it)
                        _dataState.update { State.Error }
                    }

            combine(expensesFlow, userRepository.get()) { expensesResult, account ->
                if (expensesResult.isFailure) {
                    _dataState.update {
                        State.Error
                    }
                } else if (expensesResult.getOrNull().isNullOrEmpty()) {
                    _dataState.update {
                        State.Empty
                    }
                } else {
                    _dataState.update {
                        val groupedExpenses =
                            expensesResult.getOrThrow().groupBy {
                                val localDate = it.date.toInstant().toLocalDateTime(TimeZone.currentSystemDefault())
                                "${localDate.month} ${localDate.year}"
                            }

                        State.Expenses(
                            banner = if (!account.isPlus() && groupedExpenses.isNotEmpty()) Banner.AI_CAT else null,
                            group = group,
                            groupedExpenses =
                            groupedExpenses,
                        )
                    }
                }
            }.collect {}
        }
    }

    @FutureFeature
    fun loadNextPage() {
        TODO("Implement")
    }

    sealed interface State {
        data object Loading : State

        data object Empty : State

        data object Error : State

        data class Expenses(
            val group: Group,
            val groupedExpenses: Map<String, List<Expense>>,
            val banner: Banner?,
        ) : State
    }
}
