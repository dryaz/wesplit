package app.wesplit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import app.wesplit.di.ActivityProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.messaging.messaging
import dev.icerock.moko.permissions.PermissionsController
import org.koin.core.annotation.Single

@Single
class AndroidPermissionDelegate(
    private val activityProvider: ActivityProvider,
    private val permissionsController: PermissionsController,
) : PermissionsDelegate {
    override suspend fun requestPermission(permission: Permission): Result<Unit> =
        kotlin.runCatching {
            permissionsController.providePermission(
                when (permission) {
                    Permission.PUSH -> dev.icerock.moko.permissions.Permission.REMOTE_NOTIFICATION
                },
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel.
                val name = "New expenses"
                val descriptionText = "You participating in expense"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val mChannel = NotificationChannel("expense_added", name, importance)
                mChannel.description = descriptionText
                // Register the channel with the system. You can't change the importance
                // or other notification behaviors after this.
                val notificationManager = activityProvider.activeActivity?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(mChannel)
            }

            Firebase.messaging.subscribeToTopic("expense_added")
            return Result.success(Unit)
        }
}
