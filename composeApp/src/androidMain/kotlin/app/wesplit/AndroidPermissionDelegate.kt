package app.wesplit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.annotation.RequiresApi
import app.wesplit.di.ActivityProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.messaging.messaging
import dev.icerock.moko.permissions.PermissionsController
import org.koin.core.annotation.Single

// TODO: Test on Android if updatechannels works ok when notification permission is disabled
@Single
class AndroidPermissionDelegate(
    private val activityProvider: ActivityProvider,
    private val permissionsController: PermissionsController,
) : PermissionsDelegate {
    init {
        updateChannels()
    }

    override suspend fun requestPermission(permission: Permission): Result<Unit> =
        kotlin.runCatching {
            permissionsController.providePermission(
                when (permission) {
                    Permission.PUSH -> dev.icerock.moko.permissions.Permission.REMOTE_NOTIFICATION
                },
            )

            updateChannels()
            return Result.success(Unit)
        }

    private fun updateChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = ChannelType.entries.map { getChannel(it) }
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            val notificationManager = activityProvider.activeActivity?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(channels)

            channels.forEach {
                Firebase.messaging.subscribeToTopic(it.id)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getChannel(type: ChannelType): NotificationChannel {
        // TODO: Lokalization
        val name =
            when (type) {
                ChannelType.NEW_EXPENSE -> "New expenses"
                ChannelType.RETENTION_REMINDERS -> "Reminders"
            }
        val descriptionText =
            when (type) {
                ChannelType.NEW_EXPENSE -> "You participating in expense"
                ChannelType.RETENTION_REMINDERS -> "Notification about balances"
            }
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channelId =
            when (type) {
                ChannelType.NEW_EXPENSE -> "expense_added"
                ChannelType.RETENTION_REMINDERS -> "reminder"
            }
        val mChannel = NotificationChannel(channelId, name, importance)
        mChannel.description = descriptionText
        return mChannel
    }

    private enum class ChannelType {
        NEW_EXPENSE,
        RETENTION_REMINDERS,
    }
}
