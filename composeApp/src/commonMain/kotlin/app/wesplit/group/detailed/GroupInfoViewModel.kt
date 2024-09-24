package app.wesplit.group.detailed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.account.Login
import app.wesplit.domain.model.exception.UnauthorizeAcceessException
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.expense.ExpenseDetailsViewModel.State
import app.wesplit.routing.RightPane
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class GroupInfoViewModel(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val accountRepository: AccountRepository,
    private val analyticsManager: AnalyticsManager,
) : ViewModel(),
    KoinComponent {
    val dataState: StateFlow<State>
        get() = _dataState

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

    private val _dataState = MutableStateFlow<State>(State.Loading)

    init {
        refresh()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun refresh() =
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
                        -> flow { emit(State.Loading) }

                        is Account.Authorized,
                        Account.Restricted,
                        ->
                            groupRepository.get(groupId, token).mapLatest { groupResult ->
                                val exception = groupResult.exceptionOrNull()
                                when (exception) {
                                    is UnauthorizeAcceessException -> State.Error(State.Error.Type.UNAUTHORIZED)
                                    is NullPointerException -> State.Error(State.Error.Type.NOT_EXISTS)
                                    else ->
                                        if (exception != null) {
                                            State.Error(State.Error.Type.FETCH_ERROR)
                                        } else {
                                            State.GroupInfo(groupResult.getOrThrow())
                                        }
                                }
                            }
                    }
                }
                .catch {
                    analyticsManager.log("GroupInfoViewModel - refresh()", LogLevel.WARNING)
                    analyticsManager.log(it)
                    // TODO: Improve error handling, e.g. get reason and plot proper data
                    _dataState.update {
                        State.Error(State.Error.Type.FETCH_ERROR)
                    }
                }
                .collectLatest {
                    _dataState.value = it
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

        data class GroupInfo(
            val group: Group,
        ) : State
    }
}
