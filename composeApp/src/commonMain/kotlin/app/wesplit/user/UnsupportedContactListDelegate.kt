package app.wesplit.user

import app.wesplit.domain.model.user.ContactListDelegate

class UnsupportedContactListDelegate : ContactListDelegate {
    override fun get(searchQuery: String?): ContactListDelegate.State = ContactListDelegate.State.NotSuppoted
}
