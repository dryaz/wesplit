import app.wesplit.ShortcutAction
import app.wesplit.ShortcutDelegate
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
    val shortcutItem = UIApplicationShortcutItem(
        type = "add_exp_${group.id}",
        localizedTitle = "Add Expense to ${group.uiTitle()}",
        localizedSubtitle = null,
        icon = UIApplicationShortcutIcon.iconWithType(UIApplicationShortcutIconType.UIApplicationShortcutIconTypeAdd),
        userInfo = mapOf("group_deeplink" to NSString.create(string = getDeeplink(group.id)))
    )

    // If you have existing shortcuts, append to them
    val existingShortcuts = UIApplication.sharedApplication.shortcutItems?.toMutableList() ?: mutableListOf()
    existingShortcuts.add(shortcutItem)

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
