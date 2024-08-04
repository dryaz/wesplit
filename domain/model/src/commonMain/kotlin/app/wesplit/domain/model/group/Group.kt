package app.wesplit.domain.model.group

import app.wesplit.domain.model.account.Account

data class Group(val id: String, val title: String, val users: List<Account>)
