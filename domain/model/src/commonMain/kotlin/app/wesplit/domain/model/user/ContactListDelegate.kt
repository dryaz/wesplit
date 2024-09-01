package app.wesplit.domain.model.user

interface ContactListDelegate {
    fun get(): State
}

sealed interface State {
    data object NotSuppoted : State

    data object PermissionRequired : State

    data class Contacts(val data: List<User>) : State
}
