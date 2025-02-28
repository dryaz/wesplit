package app.wesplit.settle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.balance.BalanceFxCalculationUseCase
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.account.Login
import app.wesplit.domain.model.account.isPlus
import app.wesplit.domain.model.currency.CurrencyRepository
import app.wesplit.domain.model.currency.FxState
import app.wesplit.domain.model.exception.UnauthorizeAcceessException
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.group.Balance
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.ParticipantBalance
import app.wesplit.domain.model.group.isMe
import app.wesplit.domain.model.user.UserRepository
import app.wesplit.domain.model.user.isPlus
import app.wesplit.domain.settle.SettleSuggestion
import app.wesplit.domain.settle.SettleSuggestionUseCase
import app.wesplit.routing.RightPane
import app.wesplit.ui.molecules.FxToggleState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

private const val SETTLE_ALL_EVENT = "settle_all"

private const val RECALCULATION_PAYWALL_SOURCE = "recalculation"
private const val SUGGESTIONS_PAYWALL_SOURCE = "settle_suggestions"
private const val CURRENCY_PAYWALL_SOURCE = "currency_recalc"

class SettleViewModel(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val expenseRepository: ExpenseRepository,
    private val currencyRepository: CurrencyRepository,
    private val analyticsManager: AnalyticsManager,
    private val balanceFxCalculationUseCase: BalanceFxCalculationUseCase,
    private val settleSuggestionUseCase: SettleSuggestionUseCase,
    private val onSubscriptionRequest: (String) -> Unit,
) : ViewModel(), KoinComponent {
    val uiState: StateFlow<UiState>
        get() = _uiState

    private val groupId: String =
        checkNotNull(
            savedStateHandle[
                RightPane
                    .Group
                    .Param
                    .GROUP_ID
                    .paramName,
            ],
        )

    private val token: String? =
        savedStateHandle[
            RightPane
                .Group
                .Param
                .TOKEN
                .paramName,
        ]

    private val uiSetting =
        MutableStateFlow<UiSetting>(
            UiSetting(
                selectedCurrency = "USD",
                isRecalculateEnabled = false,
                isSuggestionsEnabled = false,
            ),
        )

    private val groupState = MutableStateFlow<GroupState>(GroupState.Loading)

    private val _uiState =
        combine(
            groupState,
            currencyRepository.getFxRates(),
            currencyRepository.getAvailableCurrencyCodes(),
            uiSetting,
        ) { groupState, fxState, availableCurrencies, uiSettings ->
            if (groupState is GroupState.Loading) {
                return@combine UiState.Loading
            }

            if (groupState is GroupState.Error) {
                return@combine UiState.Error(
                    when (groupState.type) {
                        GroupState.Error.Type.NOT_EXISTS,
                        GroupState.Error.Type.FETCH_ERROR,
                        -> UiState.Error.Type.FETCH_ERROR

                        GroupState.Error.Type.UNAUTHORIZED -> UiState.Error.Type.UNAUTHORIZED
                    },
                )
            }

            check(groupState is GroupState.SettleInfo)

            val balance = checkNotNull(groupState.group.balances)

            val participantBalance: Balance =
                if (uiSettings.isRecalculateEnabled && fxState is FxState.Data) {
                    // TODO: Some loading if fxrates is still loading?
                    balanceFxCalculationUseCase.recalculate(balance, fxState.fxRates, uiSettings.selectedCurrency)
                } else {
                    balance.copy(
                        participantsBalance =
                            balance.participantsBalance.map { partBalance ->
                                partBalance.copy(
                                    participant =
                                        groupState
                                            .group
                                            .participants
                                            .firstOrNull { it == partBalance.participant } ?: partBalance.participant,
                                )
                            }.sortedWith(
                                compareByDescending<ParticipantBalance> { it.participant.isMe() }
                                    .thenBy { it.participant.name },
                            ).toSet(),
                    )
                }

            UiState.Data(
                group = groupState.group,
                fxToggleState =
                    FxToggleState(
                        fxRates = fxState,
                        selectedCurrency = uiSettings.selectedCurrency,
                        currencyCodesCollection = availableCurrencies,
                        isRecalculateEnabled = uiSettings.isRecalculateEnabled,
                    ),
                participantBalances = participantBalance,
                // TODO: Store in settings?
                suggestionsEnabled = uiSettings.isSuggestionsEnabled,
                suggestions =
                    if (uiSettings.isSuggestionsEnabled) {
                        UiState.Data.ApplicableSuggestion(
                            appliedSuggestions = settleSuggestionUseCase.get(participantBalance),
                            // TODO: IDEA is to give granular control for user
                            suggestion = emptyList(),
                        )
                    } else {
                        null
                    },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UiState.Loading,
        )

    init {
        startInternalStatesUpdates()
    }

    // TODO: To Actions?
    fun selectCurrency(currencyCode: String) {
        plusProtectedCall(CURRENCY_PAYWALL_SOURCE) {
            uiSetting.getAndUpdate {
                it.copy(selectedCurrency = currencyCode)
            }
        }
    }

    fun toggleRecalculation(isEnabled: Boolean) {
        plusProtectedCall(RECALCULATION_PAYWALL_SOURCE) {
            uiSetting.getAndUpdate {
                it.copy(isRecalculateEnabled = isEnabled)
            }
        }
    }

    fun toggleSuggestions(isEnabled: Boolean) {
        plusProtectedCall(SUGGESTIONS_PAYWALL_SOURCE) {
            uiSetting.getAndUpdate {
                it.copy(isSuggestionsEnabled = isEnabled)
            }
        }
    }

    fun settleAll() {
        analyticsManager.track(SETTLE_ALL_EVENT)
        viewModelScope.launch {
            withContext(NonCancellable) {
                expenseRepository.settle(groupId)
            }
        }
    }

    private fun plusProtectedCall(
        eventSource: String,
        call: () -> Unit,
    ) {
        if (accountRepository.get().value.isPlus()) {
            analyticsManager.track(eventSource)
            call.invoke()
        } else {
            onSubscriptionRequest(eventSource)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startInternalStatesUpdates() {
        viewModelScope.launch {
            userRepository.get().filterNotNull().collectLatest { user ->
                user.lastUsedCurrency?.let { currency ->
                    uiSetting.getAndUpdate {
                        it.copy(
                            selectedCurrency = currency,
                            isSuggestionsEnabled = user.isPlus(),
                            isRecalculateEnabled = user.isPlus(),
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            accountRepository
                .get()
                .onEach {
                    if (it is Account.Anonymous && token != null) {
                        accountRepository.login(
                            Login.GroupToken(
                                groupId = groupId,
                                token = token,
                            ),
                        )
                    }
                }
                .distinctUntilChanged()
                .flatMapLatest {
                    when (it) {
                        Account.Unknown,
                        Account.Anonymous,
                        -> flow { emit(GroupState.Loading) }

                        is Account.Authorized,
                        Account.Restricted,
                        ->
                            groupRepository.get(groupId, token).mapLatest { groupResult ->
                                val exception = groupResult.exceptionOrNull()
                                when (exception) {
                                    is UnauthorizeAcceessException -> GroupState.Error(GroupState.Error.Type.UNAUTHORIZED)
                                    is NullPointerException -> GroupState.Error(GroupState.Error.Type.NOT_EXISTS)
                                    else ->
                                        if (exception != null) {
                                            GroupState.Error(GroupState.Error.Type.FETCH_ERROR)
                                        } else {
                                            val group = groupResult.getOrThrow()
                                            GroupState.SettleInfo(group)
                                        }
                                }
                            }
                    }
                }
                .catch {
                    analyticsManager.log("SettleViewModel - refresh()", LogLevel.WARNING)
                    analyticsManager.log(it)
                    // TODO: Improve error handling, e.g. get reason and plot proper data
                    groupState.update {
                        GroupState.Error(GroupState.Error.Type.FETCH_ERROR)
                    }
                }
                .collect {
                    groupState.value = it
                }
        }
    }

    sealed interface UiState {
        data object Loading : UiState

        data class Error(val type: Type) : UiState {
            enum class Type {
                UNAUTHORIZED,
                FETCH_ERROR,
            }
        }

        data class Data(
            val group: Group,
            val fxToggleState: FxToggleState,
            val participantBalances: Balance,
            val suggestionsEnabled: Boolean,
            val suggestions: ApplicableSuggestion?,
        ) : UiState {
            data class ApplicableSuggestion(
                val appliedSuggestions: List<SettleSuggestion>,
                val suggestion: List<SettleSuggestion>,
            )
        }
    }
}

private data class UiSetting(
    val selectedCurrency: String,
    val isRecalculateEnabled: Boolean,
    val isSuggestionsEnabled: Boolean,
)

private sealed interface GroupState {
    data object Loading : GroupState

    data class Error(val type: Type) : GroupState {
        enum class Type {
            NOT_EXISTS,
            UNAUTHORIZED,
            FETCH_ERROR,
        }
    }

    data class SettleInfo(
        val group: Group,
    ) : GroupState
}
