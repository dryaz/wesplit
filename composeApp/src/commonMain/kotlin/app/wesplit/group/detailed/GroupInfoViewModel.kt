package app.wesplit.group.detailed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.FileDownloadDelegate
import app.wesplit.domain.balance.BalanceLocalCalculationUseCase
import app.wesplit.domain.export.CsvExportUseCase
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.account.Login
import app.wesplit.domain.model.account.isPlus
import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.exception.UnauthorizeAcceessException
import app.wesplit.domain.model.expense.Category
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.expense.ExpenseType
import app.wesplit.domain.model.expense.Share
import app.wesplit.domain.model.feature.Feature
import app.wesplit.domain.model.feature.FeatureAvailability
import app.wesplit.domain.model.feature.FeatureRepository
import app.wesplit.domain.model.group.Balance
import app.wesplit.domain.model.group.BalanceStatus
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.ParticipantBalance
import app.wesplit.domain.model.group.isMe
import app.wesplit.group.detailed.expense.ExpenseSectionViewModel.State
import app.wesplit.routing.RightPane
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.fromMilliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent

private const val QUICK_ADD_ACTION = "quick_add_expense"
private const val QUICK_ADD_ACTION_IN_ROW_PARAM = "add_in_row"

private const val CSV_EXPORT_EVENT = "csv_export"
private const val CSV_EXPORT_PAYWALL_EVENT = "csv_export_paywall_shown"
private const val CSV_EXPORT_INCLUDE_SHARES_PARAM = "include_shares"

class GroupInfoViewModel(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val accountRepository: AccountRepository,
    private val analyticsManager: AnalyticsManager,
    private val expenseRepository: ExpenseRepository,
    private val balanceLocalCalculationUseCase: BalanceLocalCalculationUseCase,
    private val featureRepository: FeatureRepository,
    private val csvExportUseCase: CsvExportUseCase,
    private val fileDownloadDelegate: FileDownloadDelegate,
) : ViewModel(), KoinComponent {
    val dataState: StateFlow<State>
        get() = _dataState

    val event: kotlinx.coroutines.flow.Flow<Event>
        get() = _event.receiveAsFlow()

    private val groupId: String =
        checkNotNull(
            savedStateHandle[
                RightPane.Group.Param.GROUP_ID.paramName,
            ],
        )

    private val token: String? =
        savedStateHandle[
            RightPane.Group.Param.TOKEN.paramName,
        ]

    private val _dataState = MutableStateFlow<State>(State.Loading)
    private val _event = Channel<Event>(Channel.BUFFERED)

    init {
        refresh()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun refresh() =
        viewModelScope.launch {
            accountRepository.get().onEach {
                if (it is Account.Anonymous && token != null) {
                    accountRepository.login(
                        Login.GroupToken(
                            groupId = groupId,
                            token = token,
                        ),
                    )
                }
            }.distinctUntilChanged().flatMapLatest { account ->
                when (account) {
                    Account.Unknown,
                    Account.Anonymous,
                    -> flow { emit(State.Loading) }

                    is Account.Authorized,
                    Account.Restricted,
                    ->
                        groupRepository
                            .get(groupId, token)
                            .combine(featureRepository.get(Feature.QUICK_ADD)) { groupResult, quickAddFeature ->
                                groupResult to quickAddFeature
                            }
                            .mapLatest { groupFeaturePair ->
                                val groupResult = groupFeaturePair.first
                                val exception = groupResult.exceptionOrNull()
                                when (exception) {
                                    is UnauthorizeAcceessException -> State.Error(State.Error.Type.UNAUTHORIZED)
                                    is NullPointerException -> State.Error(State.Error.Type.NOT_EXISTS)
                                    else ->
                                        if (exception != null) {
                                            State.Error(State.Error.Type.FETCH_ERROR)
                                        } else {
                                            val group = groupResult.getOrThrow().recalculateBalance(isPlus = account.isPlus())
                                            State.GroupInfo(group, groupFeaturePair.second)
                                        }
                                }
                            }
                }
            }.catch {
                analyticsManager.log("GroupInfoViewModel - refresh()", LogLevel.WARNING)
                analyticsManager.log(it)
                // TODO: Improve error handling, e.g. get reason and plot proper data
                _dataState.update {
                    State.Error(State.Error.Type.FETCH_ERROR)
                }
            }.collect {
                _dataState.value = it
            }
        }

    fun quickAdd(
        title: String,
        amount: Amount,
        inRow: Int,
    ) {
        analyticsManager.track(QUICK_ADD_ACTION, mapOf(QUICK_ADD_ACTION_IN_ROW_PARAM to inRow.toString()))
        val group = (dataState.value as? State.GroupInfo)?.group
        if (group == null) return

        val userShare = if (group.participants.size != 0) amount.value / group.participants.size else amount.value

        viewModelScope.launch(NonCancellable) {
            val expense =
                Expense(
                    id = null,
                    title = title,
                    totalAmount = amount,
                    category = Category.Magic,
                    payedBy = group.participants.find { it.isMe() } ?: group.participants.first(),
                    expenseType = ExpenseType.EXPENSE,
                    undistributedAmount = null,
                    date = Timestamp.fromMilliseconds(Clock.System.now().toEpochMilliseconds().toDouble()),
                    shares =
                        group.participants.map { participant ->
                            Share(
                                participant = participant,
                                amount = Amount(userShare, amount.currencyCode),
                            )
                        }.toSet(),
                )

            expenseRepository.commit(
                groupId = groupId,
                expense = expense,
            )
        }
    }

    private suspend fun Group.recalculateBalance(isPlus: Boolean): Group {
        val localExpenses = expenseRepository.getByGroupId(this.id).first().getOrNull()
        val tempBalance =
            this.balances ?: Balance(
                status = if (localExpenses?.size == 0) BalanceStatus.SYNC else BalanceStatus.INVALID,
            )

        val balance =
            if (tempBalance.status == BalanceStatus.INVALID && isPlus) {
                if (localExpenses != null) {
                    balanceLocalCalculationUseCase.invoke(localExpenses)
                } else {
                    tempBalance
                }
            } else {
                tempBalance
            }

        val currentBalanceParticipants = balance.participantsBalance.map { it.participant.id }
        val notReflectedParticipants =
            this.participants.filterNot {
                it.id in currentBalanceParticipants
            }
        val newParticipantBalance =
            balance.participantsBalance +
                notReflectedParticipants.map { ppl ->
                    ParticipantBalance(
                        participant = ppl,
                    )
                }
        return this.copy(
            balances =
                balance.copy(
                    participantsBalance =
                        newParticipantBalance
                            .map { partBalance ->
                                partBalance.copy(
                                    participant =
                                        this
                                            .participants
                                            .firstOrNull { it == partBalance.participant } ?: partBalance.participant,
                                )
                            }
                            .sortedWith(
                                compareByDescending<ParticipantBalance> { it.participant.isMe() }
                                    .thenBy { it.participant.name },
                            ).toSet(),
                ),
        )
    }

    fun exportCsv(includeShares: Boolean) {
        viewModelScope.launch {
            try {
                analyticsManager.track(
                    CSV_EXPORT_EVENT,
                    mapOf(CSV_EXPORT_INCLUDE_SHARES_PARAM to includeShares.toString()),
                )

                val expenses = expenseRepository.getByGroupId(groupId).first().getOrNull() ?: emptyList()
                val group = (dataState.value as? State.GroupInfo)?.group

                if (group == null) {
                    _event.send(Event.CsvExportError("Group not found"))
                    return@launch
                }

                val csvContent = csvExportUseCase.generateCsv(group, expenses, includeShares)
                val fileName = "${group.title.ifBlank { "transactions" }}_${Clock.System.now().toEpochMilliseconds()}.csv"

                val filePath =
                    fileDownloadDelegate.downloadFile(
                        fileName = fileName,
                        content = csvContent,
                        mimeType = "text/csv",
                    )

                if (filePath != null) {
                    _event.send(Event.CsvExportSuccess(filePath))
                } else {
                    _event.send(Event.CsvExportError("Failed to save file"))
                }
            } catch (e: Exception) {
                analyticsManager.log(e)
                _event.send(Event.CsvExportError(e.message ?: "Unknown error"))
            }
        }
    }

    fun requestCsvExport() {
        viewModelScope.launch {
            val isPlus = accountRepository.get().value.isPlus()
            if (isPlus) {
                _event.send(Event.ShowCsvDialog)
            } else {
                analyticsManager.track(CSV_EXPORT_PAYWALL_EVENT)
                _event.send(Event.ShowPaywall)
            }
        }
    }

    sealed interface Event {
        data object ShowCsvDialog : Event

        data object ShowPaywall : Event

        data class CsvExportSuccess(val filePath: String) : Event

        data class CsvExportError(val message: String) : Event
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

        data class GroupInfo(
            val group: Group,
            val quickAddFeature: FeatureAvailability,
        ) : State
    }
}
