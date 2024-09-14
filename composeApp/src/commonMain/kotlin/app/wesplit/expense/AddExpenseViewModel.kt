package app.wesplit.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent

sealed interface UpdateAction {
    // TODO: Update currency, FX feature and paywall - only for payed
    data class Title(val title: String) : UpdateAction

    data class TotalAmount(val value: Float) : UpdateAction

    data object Commit : UpdateAction

    sealed interface Split : UpdateAction {
        data class Equal(val participant: Participant, val isIncluded: Boolean) : Split
    }
}

class AddExpenseViewModel(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val analyticsManager: AnalyticsManager,
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
        viewModelScope.launch {
            groupRepository.get(groupId).collectLatest { groupResult ->
                if (groupResult.isFailure) {
                    groupResult.exceptionOrNull()?.let {
                        analyticsManager.log(it)
                    }
                    // TODO: Check how to define error type.
                    // TODO: Error type should be at least base on 'caues of fetch_error, unauth, not_exists
                    _state.update { State.Error(State.Error.Type.FETCH_ERROR) }
                } else {
                    val group = groupResult.getOrNull()
                    if (group != null) {
                        _state.update {
                            State.Data(
                                group = group,
                                expense =
                                    Expense(
                                        id = null,
                                        title = "",
                                        payedBy = group.participants.find { it.isMe } ?: group.participants.first(),
                                        // TODO: Currency model/set not to have hardcoded USD, map to sybmol etc
                                        totalAmount = Amount(0f, "USD"),
                                        shares =
                                            group.participants.map { participant ->
                                                Share(
                                                    participant = participant,
                                                    // TODO: Currency should be first defined on the group level to have base currency for the group
                                                    //  and in future implement FX. Meanwhile disable currency chooser on expense lvl.
                                                    amount = Amount(0f, "USD"),
                                                )
                                            }.toSet(),
                                        date = Clock.System.now(),
                                        expenseType = ExpenseType.EXPENSE,
                                    ),
                            )
                        }
                    } else {
                        // TODO: Maybe we could have extension toData(?) which do fetch_error, not exists and other errors, log and/or return data
                        _state.update { State.Error(State.Error.Type.NOT_EXISTS) }
                    }
                }
            }
        }
    }

    fun update(action: UpdateAction) {
        val currentData = (_state.value as? State.Data)
        currentData?.let { data ->
            val expense = data.expense
            when (action) {
                is UpdateAction.Title -> _state.update { data.copy(expense = expense.copy(title = action.title)) }
                is UpdateAction.TotalAmount ->
                    _state.update {
                        data.copy(
                            expense = calculateShares(expense.copy(totalAmount = expense.totalAmount.copy(value = action.value))),
                        )
                    }

                is UpdateAction.Split.Equal -> _state.update { data.copy(expense = calculateShares(expense, action)) }

                UpdateAction.Commit ->
                    (_state.value as? State.Data)?.expense?.let { exp ->
                        expenseRepository.addExpense(groupId, exp)
                        // TODO: should we check for success event from here to close the screen of Firebase could handle it properly
                        //  saving first in local and only then pushing to remote?
                    }
            }
        } ?: {
            // TODO: Show error on UI
            analyticsManager.log("Try to perform $action when current stats is yet ${_state.value}", LogLevel.ERROR)
        }
    }

    // TODO: Extract to usecase, cover by tests
    // TODO: When new split option supported -> need to use base UpdateCation.Split and do different calculations
    private fun calculateShares(
        expense: Expense,
        action: UpdateAction.Split.Equal? = null,
    ): Expense {
        val sum = expense.totalAmount.value
        val currency = expense.totalAmount.currencyCode
        val currentParticiapants =
            (expense.shares).filter {
                if (it.participant == action?.participant) {
                    action.isIncluded
                } else {
                    true
                }
            }.map { it.participant }.toHashSet()

        val totalParticipants =
            action?.let {
                if (it.isIncluded) currentParticiapants + it.participant else currentParticiapants
            } ?: currentParticiapants

        // TODO: It will fail in some case, maaaybe need to use bigdecimal etc.
        val sharePerPart = sum / totalParticipants.size
        return expense.copy(
            shares =
                totalParticipants.map {
                    Share(
                        participant = it,
                        amount =
                            Amount(
                                value = sharePerPart,
                                currencyCode = currency,
                            ),
                    )
                }.toSet(),
        )
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
