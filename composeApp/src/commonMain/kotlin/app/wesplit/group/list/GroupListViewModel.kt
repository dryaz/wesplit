package app.wesplit.group.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.user.UserRepository
import app.wesplit.domain.model.user.isPlus
import app.wesplit.ui.Banner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class GroupListViewModel(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val ioDispatcher: CoroutineDispatcher,
    private val analyticsManager: AnalyticsManager,
) : ViewModel(),
    KoinComponent {
    val dataState: StateFlow<State>
        get() = _dataState

    val accountState: StateFlow<Account>
        get() = accountRepository.get()

    private val _dataState = MutableStateFlow<State>(State.Empty)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            groupRepository.get()
                .catch {
                    analyticsManager.log("GropuListViewModel - refresh()", LogLevel.WARNING)
                    analyticsManager.log(it)
                    _dataState.update { State.Empty }
                }
                .combine(userRepository.get()) { groups, user ->
                    _dataState.update {
                        if (groups.isEmpty()) {
                            State.Empty
                        } else {
                            State.Groups(
                                groups = groups,
                                banner = if ((user?.isPlus() ?: true) && groups.size > 0) null else Banner.IMG_GROUP,
                            )
                        }
                    }
                }
                .collect { }
        }
    }

    sealed interface State {
        data object Empty : State

        data class Groups(
            val groups: List<Group>,
            val banner: Banner?,
        ) : State
    }
}
