package app.wesplit.expense.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.account.isPlus
import app.wesplit.domain.model.expense.Category
import app.wesplit.domain.model.expense.categories
import app.wesplit.domain.model.expense.freeCategories
import korlibs.io.async.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent

class ExpenseCategoryPickerViewModel(
    val accountRepository: AccountRepository,
) : ViewModel(), KoinComponent {
    val state = MutableStateFlow<State>(State.Loading)

    init {
        viewModelScope.launch {
            accountRepository.get().collectLatest { account ->
                val unlocked =
                    if (account.isPlus()) {
                        Category.entries.toSet()
                    } else {
                        freeCategories
                    }

                state.update {
                    State.Data(
                        selected = Category.None,
                        categories = categories,
                        unlocked = unlocked,
                    )
                }
            }
        }
    }

    sealed interface State {
        data object Loading : State

        data class Data(
            val selected: Category,
            val categories: Map<Category, List<Category>>,
            val unlocked: Set<Category>,
        ) : State
    }
}
