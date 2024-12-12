import app.wesplit.domain.model.ShortcutAction
import app.wesplit.domain.model.ShortcutDelegate
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.uiTitle
import app.wesplit.routing.DeeplinkAction
import app.wesplit.routing.DeeplinkBuilders
import com.motorro.keeplink.deeplink.deepLink
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationShortcutIcon
import platform.UIKit.UIApplicationShortcutIconType
import platform.UIKit.UIApplicationShortcutItem
import platform.UIKit.shortcutItems

class ShortcutIosDelegate : ShortcutDelegate {
    override fun push(action: ShortcutAction) {
        when (action) {
            is ShortcutAction.NewExpense -> createAddExpenseShortcut(action.group)
            // Handle other actions if necessary
        }
    }
}

@OptIn(BetaInteropApi::class)
private fun createAddExpenseShortcut(group: Group) {
    val shortcutItem =
        UIApplicationShortcutItem(
            type = "add_exp_${group.id}",
            localizedTitle = "Add Expense to ${group.uiTitle()}",
            localizedSubtitle = null,
            icon = UIApplicationShortcutIcon.iconWithType(UIApplicationShortcutIconType.UIApplicationShortcutIconTypeAdd),
            userInfo =
                mapOf(
                    NSString.create(string = "group_deeplink") to NSString.create(string = getDeeplink(group.id)),
                ),
        )

    // Retrieve existing shortcuts or initialize an empty list
    val existingShortcuts = UIApplication.sharedApplication.shortcutItems?.toMutableList() ?: mutableListOf()

    // Remove any existing shortcut with the same type
    // existingShortcuts.removeAll { it.type == shortcutItem.type }
    existingShortcuts.clear()

    // Add the new shortcut
    existingShortcuts.add(shortcutItem)

    // Ensure the maximum number of shortcuts is 4 by removing the oldest ones if necessary
    if (existingShortcuts.size > 4) {
        existingShortcuts.removeFirst()
    }

    UIApplication.sharedApplication.shortcutItems = existingShortcuts
}

private fun getDeeplink(groupId: String): String {
    val detailsAction =
        DeeplinkAction.Group.Expense(
            groupId = groupId,
            expenseId = null,
        )

    val link = deepLink(detailsAction)
    return DeeplinkBuilders.PROD.build(link)
}
