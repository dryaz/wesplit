package app.wesplit.user

import app.wesplit.domain.model.user.ContactListDelegate
import app.wesplit.domain.model.user.State

class UnsupportedContactListDelegate: ContactListDelegate {
    override fun get(): State = State.NotSuppoted
}
