package app.wesplit

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.uiTitle
import app.wesplit.routing.DeeplinkAction
import app.wesplit.routing.DeeplinkBuilders
import com.motorro.keeplink.deeplink.deepLink

private const val ADD_EXPENSE_PREFIX = "add_exp_"

const val GROUP_ID_KEY = "group_id_key"

class ShortcutAndroidDelegate(
    private val context: Context,
) : ShortcutDelegate {
    companion object {
        fun getDeeplink(intent: Intent): String? {
            val groupId = intent.getStringExtra(GROUP_ID_KEY) ?: return null

            val detailsAction =
                DeeplinkAction.Group.Expense(
                    groupId = groupId,
                    expenseId = null,
                )

            val link = deepLink(detailsAction)
            return DeeplinkBuilders.PROD.build(link)
        }
    }

    override fun push(action: ShortcutAction) =
        when (action) {
            is ShortcutAction.NewExpense -> createAddExpenseShortcut(context, action.group)
        }
}

private fun createAddExpenseShortcut(
    context: Context,
    group: Group,
) {
    // Create an intent that launches the specific activity when the shortcut is pressed
    val shortcutIntent =
        Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
        }

    shortcutIntent.putExtra(GROUP_ID_KEY, group.id)

    // Build the shortcut
    val shortcut =
        ShortcutInfoCompat.Builder(context, "$ADD_EXPENSE_PREFIX${group.id}")
            .setShortLabel(context.getString(R.string.shortcut_add_expense_short, group.uiTitle()))
            .setLongLabel(context.getString(R.string.shortcut_add_expense_long, group.uiTitle()))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_add_money)) // Set your own icon here
            .setIntent(shortcutIntent)
            .build()

    // Push the dynamic shortcut
    ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
}
