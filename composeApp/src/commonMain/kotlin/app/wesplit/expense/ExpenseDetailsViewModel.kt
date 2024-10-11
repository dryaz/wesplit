package app.wesplit.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.ShortcutAction
import app.wesplit.ShortcutDelegate
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.AppReviewManager
import app.wesplit.domain.model.LogLevel
import app.wesplit.domain.model.REVIEW_EVENT
import app.wesplit.domain.model.REVIEW_SOURCE
import app.wesplit.domain.model.REVIEW_TYPE
import app.wesplit.domain.model.ReviewType
import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.currency.CurrencyCodesCollection
import app.wesplit.domain.model.currency.CurrencyRepository
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.expense.ExpenseType
import app.wesplit.domain.model.expense.Share
import app.wesplit.domain.model.expense.SplitType
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.isMe
import app.wesplit.routing.RightPane
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.fromMilliseconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent

private const val EXPENSE_COMMIT_COUNTER_KEY = "ex_com"

private const val UPDATE_TITLE_EVENT = "exp_update_title"
private const val UPDATE_DATE_EVENT = "exp_update_date"
private const val UPDATE_AMOUNT_EVENT = "exp_update_amount"
private const val UPDATE_PAYER_EVENT = "exp_update_payer"
private const val UPDATE_SHARES_EVENT = "exp_update_shares"

private const val UPDATE_PROTECTION = "exp_update_protection"

sealed interface UpdateAction {
    // TODO: Update currency, FX feature and paywall - only for payed
    data class Title(val title: String) : UpdateAction

    data class TotalAmount(val value: Double, val currencyCode: String) : UpdateAction

    data class Date(val millis: Long) : UpdateAction

    data object Commit : UpdateAction

    data object Delete : UpdateAction

    data class NewPayer(val participant: Participant) : UpdateAction

    data class Protect(val isProtected: Boolean) : UpdateAction

    sealed interface Split : UpdateAction {
        abstract val participant: Participant
        abstract val value: Any

        data class Equal(override val participant: Participant, override val value: Boolean) : Split

        data class Share(override val participant: Participant, override val value: Double) : Split

        data class Amount(override val participant: Participant, override val value: Double) : Split
    }
}

class ExpenseDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val currencyRepository: CurrencyRepository,
    private val analyticsManager: AnalyticsManager,
    private val shortcutDelegate: ShortcutDelegate,
    private val settings: Settings,
    private val appReviewManager: AppReviewManager,
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

            val currencyFlow = currencyRepository.getAvailableCurrencyCodes()

            combine(
                groupRepository.get(groupId),
                expenseFlow,
                currencyFlow,
            ) { groupResult, expenseResult, currencies ->
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

                    val existingExpense = expenseResult.getOrNull()
                    if (existingExpense == null) {
                        shortcutDelegate.push(ShortcutAction.NewExpense(group))
                    }

                    val currencyCode = currencyFlow.value.lru.first()
                    val expense =
                        existingExpense ?: Expense(
                            id = null,
                            title = "",
                            payedBy = group.participants.find { it.isMe() } ?: group.participants.first(),
                            // TODO: Currency model/set not to have hardcoded USD, map to sybmol etc
                            totalAmount = Amount(0.0, currencyCode),
                            shares =
                                group.participants.map { participant ->
                                    Share(
                                        participant = participant,
                                        // TODO: Currency should be first defined on the group level to have base currency for the group
                                        //  and in future implement FX. Meanwhile disable currency chooser on expense lvl.
                                        amount = Amount(0.0, currencyCode),
                                    )
                                }.toSet(),
                            expenseType = ExpenseType.EXPENSE,
                            undistributedAmount = null,
                            date = Timestamp.fromMilliseconds(Clock.System.now().toEpochMilliseconds().toDouble()),
                        )

                    val expenseParticipants = expense.shares.map { it.participant }.toHashSet()
                    val extraParticipants = group.participants.filter { it !in expenseParticipants }

                    return@combine State.Data(
                        group = group,
                        expense = expense,
                        isComplete = isComplete(expense),
                        splitOptions = expense.getInitialSplitOptions(extraParticipants),
                        availableCurrencies = currencies,
                    )
                }
            }
                .catch {
                    analyticsManager.log(it)
                    // TODO: Improve error handling, e.g. get reason and plot proper data
                    _state.update { State.Error(State.Error.Type.FETCH_ERROR) }
                }
                .collect { state ->
                    _state.update { state }
                }
        }
    }

    fun update(action: UpdateAction) {
        val currentData = (_state.value as? State.Data)
        currentData?.let { data ->
            val expense = data.expense
            when (action) {
                is UpdateAction.Title -> {
                    analyticsManager.track(UPDATE_TITLE_EVENT)
                    _state.update { data.copy(expense = expense.copy(title = action.title)) }
                }
                is UpdateAction.Date -> {
                    analyticsManager.track(UPDATE_DATE_EVENT)
                    _state.update {
                        data.copy(
                            expense =
                                expense.copy(
                                    date = Timestamp.fromMilliseconds(action.millis.toDouble()),
                                ),
                        )
                    }
                }

                is UpdateAction.TotalAmount -> {
                    analyticsManager.track(UPDATE_AMOUNT_EVENT)
                    _state.update {
                        val newSplitOptions = data.splitOptions.update(action)

                        data.copy(
                            expense =
                                expense.copy(
                                    totalAmount = expense.totalAmount.copy(value = action.value, currencyCode = action.currencyCode),
                                ).reCalculateShares(newSplitOptions),
                            splitOptions = newSplitOptions,
                        )
                    }
                }

                is UpdateAction.Split -> {
                    analyticsManager.track(UPDATE_SHARES_EVENT)
                    _state.update {
                        val newSplitOptions = data.splitOptions.update(action)

                        data.copy(
                            expense = expense.reCalculateShares(newSplitOptions),
                            splitOptions = newSplitOptions,
                        )
                    }
                }

                UpdateAction.Delete ->
                    (_state.value as? State.Data)?.expense?.let { exp ->
                        viewModelScope.launch {
                            expenseRepository.delete(groupId, exp)
                        }
                    }

                UpdateAction.Commit ->
                    (_state.value as? State.Data)?.expense?.let { exp ->
                        viewModelScope.launch {
                            val commitedExpenses = settings.get<Int>(EXPENSE_COMMIT_COUNTER_KEY) ?: 0
                            if ((commitedExpenses + 1) % 4 == 0) {
                                appReviewManager.requestReview(ReviewType.IN_APP)
                                analyticsManager.track(
                                    REVIEW_EVENT,
                                    mapOf(
                                        REVIEW_SOURCE to "expense_create",
                                        REVIEW_TYPE to ReviewType.IN_APP.name,
                                    ),
                                )
                            }
                            settings.putInt(EXPENSE_COMMIT_COUNTER_KEY, commitedExpenses + 1)
                            expenseRepository.commit(groupId, exp)
                        }
                        // TODO: should we check for success event from here to close the screen of Firebase could handle it properly
                        //  saving first in local and only then pushing to remote?
                    }

                is UpdateAction.NewPayer -> {
                    analyticsManager.track(UPDATE_PAYER_EVENT)
                    _state.update { data.copy(expense = expense.copy(payedBy = action.participant)) }
                }

                is UpdateAction.Protect -> {
                    analyticsManager.track(UPDATE_PROTECTION)
                    val protectionList =
                        if (action.isProtected) {
                            expense.protectionList + Firebase.auth.currentUser?.uid
                        } else {
                            expense.protectionList - Firebase.auth.currentUser?.uid
                        }

                    _state.update { data.copy(expense = expense.copy(protectionList = protectionList.filterNotNull().toSet())) }
                }
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

    private fun isComplete(expense: Expense) = !expense.title.isNullOrBlank() && expense.totalAmount.value != 0.0

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
            val splitOptions: SplitOptions,
            val availableCurrencies: CurrencyCodesCollection,
        ) : State {
            data class SplitOptions(
                val selectedSplitType: SplitType,
                val splitValues: Map<SplitType, Map<Participant, Any>>,
            )
        }

        fun Data.allParticipants(): Set<Participant> {
            val participantsIds = group.participants.map { it.id }
            return group.participants + expense.shares.map { it.participant }.filterNot { it.id in participantsIds }
        }
    }
}
