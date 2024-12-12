package app.wesplit.domain.model

import app.wesplit.domain.model.group.Group

interface ShortcutDelegate {
    fun push(action: ShortcutAction)
}

sealed interface ShortcutAction {
    data class NewExpense(val group: Group) : ShortcutAction
}

object ShortcutDelegateNotSupport : ShortcutDelegate {
    override fun push(action: ShortcutAction) {
        println("Not yet supported")
    }
}
