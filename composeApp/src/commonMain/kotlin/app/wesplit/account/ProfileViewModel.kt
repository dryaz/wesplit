package app.wesplit.account

import androidx.lifecycle.ViewModel
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent

class ProfileViewModel(
    private val accountRepository: AccountRepository,
) : ViewModel(), KoinComponent {
    val accountState: StateFlow<Account>
        get() = accountRepository.get()

    fun deleteAccount() {
        accountRepository.deleteAccount()
    }
}
