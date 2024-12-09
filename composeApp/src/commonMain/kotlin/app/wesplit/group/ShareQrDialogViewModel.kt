package app.wesplit.group

import androidx.lifecycle.ViewModel
import app.wesplit.domain.model.group.GroupRepository
import org.koin.core.component.KoinComponent

class ShareQrDialogViewModel(
    private val groupRepository: GroupRepository,
) : ViewModel(), KoinComponent {
    init {
    }

    sealed interface State {
        data object Loading : State

        data class Data(
            val link: String,
        ) : State
    }
}
