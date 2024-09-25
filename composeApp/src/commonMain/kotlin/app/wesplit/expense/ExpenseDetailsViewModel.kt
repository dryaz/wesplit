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
import app.wesplit.domain.model.group.isMe
import app.wesplit.routing.RightPane
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.fromMilliseconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent

sealed interface UpdateAction {
    // TODO: Update currency, FX feature and paywall - only for payed
    data class Title(val title: String) : UpdateAction

    data class TotalAmount(val value: Float) : UpdateAction

    data class Date(val millis: Long) : UpdateAction

    data object Commit : UpdateAction

    data object Delete : UpdateAction

    data class NewPayer(val participant: Participant) : UpdateAction

    sealed interface Split : UpdateAction {
        data class Equal(val participant: Participant, val isIncluded: Boolean) : Split
    }
}

class ExpenseDetailsViewModel(
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
                    .ExpenseDetails
                    .Param
                    .GROUP_ID
                    .paramName,
            ],
        )

    // TODO: savedStateHandle should be used to support editing expense
    val expenseId: String? =
        savedStateHandle[
            RightPane
                .ExpenseDetails
                .Param
                .EXPENSE_ID
                .paramName,
        ]

    val state: StateFlow<State>
        get() = _state

    private val _state = MutableStateFlow<State>(State.Loading)

    init {
        viewModelScope.launch {
            val expenseFlow =
                if (expenseId != null) {
                    expenseRepository.getById(groupId, expenseId)
                } else {
                    flow<Result<Expense?>> {
                        emit(
                            Result.success(null),
                        )
                    }
                }

            groupRepository.get(groupId).combine(expenseFlow) { groupResult, expenseResult ->
                if (groupResult.isFailure || expenseResult.isFailure) {
                    groupResult.exceptionOrNull()?.let {
                        analyticsManager.log(it)
                    }

                    expenseResult.exceptionOrNull()?.let {
                        analyticsManager.log(it)
                    }

                    return@combine State.Error(State.Error.Type.FETCH_ERROR)
                } else {
                    val group = groupResult.getOrNull()

                    if (group == null) {
                        return@combine State.Error(State.Error.Type.NOT_EXISTS)
                    }

                    val expense =
                        expenseResult.getOrNull() ?: Expense(
                            id = null,
                            title = "",
                            payedBy = group.participants.find { it.isMe() } ?: group.participants.first(),
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
                            expenseType = ExpenseType.EXPENSE,
                            undistributedAmount = null,
                            date = Timestamp.fromMilliseconds(Clock.System.now().toEpochMilliseconds().toDouble()),
                        )

                    return@combine State.Data(
                        group = group,
                        expense = expense,
                        isComplete = isComplete(expense),
                    )
                }
            }
                .catch {

                    analyticsManager.log(it)
                    // TODO: Improve error handling, e.g. get reason and plot proper data
                    _state.update { State.Error(State.Error.Type.FETCH_ERROR) }
                }
                .collectLatest { state ->
                    _state.update { state }
                }
        }
    }

    fun update(action: UpdateAction) {
        val currentData = (_state.value as? State.Data)
        currentData?.let { data ->
            val expense = data.expense
            when (action) {
                is UpdateAction.Title -> _state.update { data.copy(expense = expense.copy(title = action.title)) }
                is UpdateAction.Date ->
                    _state.update {
                        data.copy(
                            expense =
                                expense.copy(
                                    date = Timestamp.fromMilliseconds(action.millis.toDouble()),
                                ),
                        )
                    }

                is UpdateAction.TotalAmount ->
                    _state.update {
                        data.copy(
                            expense = calculateShares(expense.copy(totalAmount = expense.totalAmount.copy(value = action.value))),
                        )
                    }

                is UpdateAction.Split.Equal -> _state.update { data.copy(expense = calculateShares(expense, action)) }

                UpdateAction.Delete ->
                    (_state.value as? State.Data)?.expense?.let { exp ->
                        viewModelScope.launch {
                            expenseRepository.delete(groupId, exp)
                        }
                    }

                UpdateAction.Commit ->
                    (_state.value as? State.Data)?.expense?.let { exp ->
                        viewModelScope.launch {
                            expenseRepository.commit(groupId, exp)
                        }
                        // TODO: should we check for success event from here to close the screen of Firebase could handle it properly
                        //  saving first in local and only then pushing to remote?
                    }

                is UpdateAction.NewPayer -> _state.update { data.copy(expense = expense.copy(payedBy = action.participant)) }
            }
        } ?: {
            // TODO: Show error on UI
            analyticsManager.log("Try to perform $action when current stats is yet ${_state.value}", LogLevel.ERROR)
        }
        validateComletion()
    }

    private fun validateComletion() {
        (_state.value as? State.Data)?.let { data ->
            _state.update {
                data.copy(
                    isComplete = isComplete(data.expense),
                )
            }
        }
    }

    private fun isComplete(expense: Expense) = !expense.title.isNullOrBlank() && expense.totalAmount.value != 0f

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
                if (it.participant.id == action?.participant?.id) {
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
        val shares =
            totalParticipants.map {
                Share(
                    participant = it,
                    amount =
                        Amount(
                            value = sharePerPart,
                            currencyCode = currency,
                        ),
                )
            }.toSet()
        val distributed = shares.map { it.amount.value }.sum()
        val residual = sum - distributed
        val undistributed = if (residual != 0f) Amount(residual, currency) else null

        return expense.copy(
            shares = shares,
            undistributedAmount = undistributed,
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
            val isComplete: Boolean,
        ) : State

        fun Data.allParticipants(): Set<Participant> {
            val participantsIds = group.participants.map { it.id }
            return group.participants + expense.shares.map { it.participant }.filterNot { it.id in participantsIds }
        }
    }
}
