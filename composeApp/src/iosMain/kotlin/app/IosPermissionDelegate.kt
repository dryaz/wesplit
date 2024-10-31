package app

import app.wesplit.Permission
import app.wesplit.PermissionsDelegate
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.messaging.messaging
import dev.icerock.moko.permissions.ios.PermissionsController

object IosPermissionDelegate : PermissionsDelegate {
    override suspend fun requestPermission(permission: Permission): Result<Unit> =
        kotlin.runCatching {
            PermissionsController().providePermission(
                when (permission) {
                    Permission.PUSH -> dev.icerock.moko.permissions.Permission.REMOTE_NOTIFICATION
                },
            )
            Firebase.messaging.subscribeToTopic("expense_added")
            return Result.success(Unit)
        }
}
