package app.wesplit.group.detailed.balance

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import app.wesplit.domain.model.expense.format
import app.wesplit.domain.model.group.balance.Balance

@Composable
fun BalanceList(balance: Balance?) {
    if (balance != null) {
        Column {
            balance.participants.forEach {
                Text("${it.key.name}: ${it.value.balance.format()}")
            }
            Text("Undistributed: ${balance.nonDistributed.format()}")
        }
    } else {
        Text("Empty balances")
    }
}
