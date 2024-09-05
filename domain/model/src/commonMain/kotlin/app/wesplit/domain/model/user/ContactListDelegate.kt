package app.wesplit.domain.model.user

import app.wesplit.domain.model.group.Participant

interface ContactListDelegate {
    // TODO: Check for android is it return flow or what?
    fun get(searchQuery: String? = null): State

    sealed interface State {
        data object NotSuppoted : State

        data object PermissionRequired : State

        data class Contacts(val data: List<Participant>) : State
    }
}
