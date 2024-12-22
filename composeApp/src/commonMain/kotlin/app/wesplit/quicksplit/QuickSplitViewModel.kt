package app.wesplit.quicksplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.isMe
import app.wesplit.domain.model.user.UserRepository
import app.wesplit.domain.model.user.participant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val QS_UPDATE_CURRENCY_EVENT = "qs_up_cur"
private const val QS_REMOVE_ITEM_EVENT = "qs_rm_item"
private const val QS_RESET = "qs_reset"

private const val QS_UPDATE_SHARE_PART = "qs_update_shares"
private const val QS_UPDATE_SHARE_PART_PPL = "ppl"
private const val QS_UPDATE_SHARE_PART_VAL = "val"

// TODO: Show sheet/screen at the end with split by person
//  List of participants, each item could expand and show all items, multipliers and sum user ows.
// TODO: Tests!
sealed interface UpdateAction {
    data class UpdateAmountValue(val value: Double) : UpdateAction

    data class UpdateAmountCurrency(val currencyCode: String) : UpdateAction

    data class UpdateExpenseParticipants(val newParticipants: Set<Participant>) : UpdateAction

    data class UpdateShareParticipants(
        val share: QuickSplitViewModel.State.Data.ShareItem,
        val participants: Set<Participant>,
        val shareDx: Int,
    ) : UpdateAction

    data class UpdateSelectedParticipants(val participants: Set<Participant>) : UpdateAction

    data class RemoveItem(val item: QuickSplitViewModel.State.Data.ShareItem) : UpdateAction

    data class AddItem(
        val share: QuickSplitViewModel.State.Data.ShareItem,
        val participants: Set<Participant>,
    ) : UpdateAction
}

class QuickSplitViewModel(
    private val userRepository: UserRepository,
    private val analyticsManager: AnalyticsManager,
) : ViewModel(), KoinComponent {
    val state: StateFlow<State>
        get() = _state

    private val _state = MutableStateFlow<State>(State.Data())

    init {
        viewModelScope.launch {
            userRepository.get().filterNotNull().collectLatest { user ->
                _state.getAndUpdate {
                    val dataState = it.dataState()
                    val newState =
                        dataState.copy(
                            amount = dataState.amount.copy(currencyCode = user.lastUsedCurrency ?: dataState.amount.currencyCode),
                            participants = setOf(user.participant()),
                            selectedParticipants = setOf(user.participant()),
                        )
                    return@getAndUpdate newState
                }
            }
        }
    }

    fun update(action: UpdateAction) {
        val dataState = _state.value.dataState()
        val newDataState =
            when (action) {
                is UpdateAction.UpdateExpenseParticipants -> {
                    dataState.copy(
                        participants = action.newParticipants,
                        selectedParticipants = action.newParticipants,
                        items =
                            dataState.items.mapValues { (_, participantMap) ->
                                participantMap.filterKeys { it in action.newParticipants }
                            },
                    )
                }

                is UpdateAction.UpdateShareParticipants -> {
                    analyticsManager.track(
                        QS_UPDATE_SHARE_PART,
                        mapOf(
                            QS_UPDATE_SHARE_PART_PPL to action.participants.size.toString(),
                            QS_UPDATE_SHARE_PART_VAL to action.shareDx.toString(),
                        ),
                    )
                    dataState.copy(
                        items =
                            dataState.items.mapValues { (shareItem, participantMap) ->
                                if (shareItem == action.share) {
                                    // Combine existing and new participants
                                    (participantMap.keys + action.participants).associateWith { participant ->
                                        val currentShare = participantMap[participant] ?: 0
                                        if (participant in action.participants) {
                                            currentShare + action.shareDx
                                        } else {
                                            currentShare
                                        }
                                    }
                                } else {
                                    participantMap
                                }
                            },
                    )
                }

                is UpdateAction.AddItem -> {
                    val newItem =
                        mapOf(
                            action.share to action.participants.associateWith { 1 },
                        )
                    dataState.copy(items = dataState.items + newItem)
                }

                is UpdateAction.RemoveItem -> {
                    analyticsManager.track(QS_REMOVE_ITEM_EVENT)
                    dataState.copy(
                        items = dataState.items.filterKeys { it != action.item },
                    )
                }

                is UpdateAction.UpdateAmountCurrency -> {
                    analyticsManager.track(QS_UPDATE_CURRENCY_EVENT)
                    dataState.copy(
                        amount = dataState.amount.copy(currencyCode = action.currencyCode),
                    )
                }

                is UpdateAction.UpdateAmountValue ->
                    dataState.copy(
                        amount = dataState.amount.copy(value = action.value),
                    )

                is UpdateAction.UpdateSelectedParticipants ->
                    dataState.copy(
                        selectedParticipants = action.participants,
                    )
            }

        // Recalculate undistributed value
        val totalDistributed =
            newDataState.items.map { (shareItem, participantMap) ->
                val totalShares = participantMap.values.sum()
                if (totalShares > 0) {
                    shareItem.priceValue
                } else {
                    0.0
                }
            }.sum()

        val recalculatedUndistributedValue =
            if (newDataState.amount.value == 0.0) {
                0.0
            } else {
                newDataState.amount.value - totalDistributed
            }

        // Filter participants with zero shares
        val cleanedItems =
            newDataState.items.mapValues { (_, participantMap) ->
                participantMap.filterValues { share -> share > 0 }
            }

        _state.update {
            newDataState.copy(
                undistributedValue = recalculatedUndistributedValue,
                items = cleanedItems,
            )
        }
    }

    fun reset() {
        analyticsManager.track(QS_RESET)
        val currentUser = _state.value.dataState().participants.find { it.isMe() }
        _state.update {
            State.Data(
                participants = currentUser?.let { setOf(it) } ?: emptySet(),
                selectedParticipants = currentUser?.let { setOf(it) } ?: emptySet(),
            )
        }
    }

    sealed interface State {
        data object Loading : State

        data class Data(
            val amount: Amount = Amount(0.0, "USD"),
            val undistributedValue: Double = 0.0,
            val participants: Set<Participant> = emptySet(),
            val selectedParticipants: Set<Participant> = emptySet(),
            val items: Map<ShareItem, Map<Participant, Int>> = emptyMap(),
        ) : State {
            data class ShareItem
                @OptIn(ExperimentalUuidApi::class)
                constructor(
                    val id: String = Uuid.random().toString(),
                    val title: String,
                    val priceValue: Double,
                )
        }
    }

    private fun State.dataState() = (this as? State.Data) ?: State.Data()
}
