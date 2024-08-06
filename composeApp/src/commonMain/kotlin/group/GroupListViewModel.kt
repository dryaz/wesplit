package group

import androidx.lifecycle.ViewModel
import app.wesplit.domain.model.account.AccountRepository
import org.koin.core.component.KoinComponent

class GroupListViewModel(private val accountRepository: AccountRepository) :
    ViewModel(),
    KoinComponent {

    fun get(): String = accountRepository.get()
}
