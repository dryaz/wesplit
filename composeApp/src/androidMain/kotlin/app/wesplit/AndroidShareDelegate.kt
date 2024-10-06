package app.wesplit

import android.content.Intent
import android.net.Uri
import app.wesplit.di.ActivityProvider
import app.wesplit.domain.model.AnalyticsManager
import org.koin.core.annotation.Single

@Single
class AndroidShareDelegate(
    private val activityProvider: ActivityProvider,
    private val analyticsManager: AnalyticsManager,
) : ShareDelegate {
    override fun supportPlatformSharing(): Boolean = true

    override fun share(data: ShareData) {
        when (data) {
            is ShareData.Link -> {
                var share = Intent(Intent.ACTION_SEND)
                share.setType("text/plain")
                // TODO: Provide group name like "[GroupName] on Wesplit"
                share.putExtra(Intent.EXTRA_SUBJECT, "Here's the group on Wesplit!")
                share.putExtra(Intent.EXTRA_TEXT, data.value)

                activityProvider.activeActivity?.startActivity(Intent.createChooser(share, "Share link!"))
            }
        }
    }

    override fun open(data: ShareData) {
        when (data) {
            is ShareData.Link -> {
                var linkIntent = Intent(Intent.ACTION_VIEW)
                linkIntent.setData(Uri.parse(data.value))
                activityProvider.activeActivity?.startActivity(linkIntent)
            }
        }
    }
}
