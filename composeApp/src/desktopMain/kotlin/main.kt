import android.app.Application
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.wesplit.App
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.account.LoginDelegate
import app.wesplit.domain.model.user.ContactListDelegate
import app.wesplit.user.UnsupportedContactListDelegate
import com.google.firebase.Firebase
import com.google.firebase.FirebaseOptions
import com.google.firebase.FirebasePlatform
import com.google.firebase.initialize
import org.koin.dsl.module
import java.awt.Dimension

fun main() {
    // TODO: In-memory only impl, need to persist
    //  TBD how to auth ???
    FirebasePlatform.initializeFirebasePlatform(
        object : FirebasePlatform() {
            val storage = mutableMapOf<String, String>()

            override fun store(
                key: String,
                value: String,
            ) = storage.set(key, value)

            override fun retrieve(key: String) = storage[key]

            override fun clear(key: String) {
                storage.remove(key)
            }

            override fun log(msg: String) = println(msg)
        },
    )

    var options: FirebaseOptions =
        FirebaseOptions.Builder()
            .setProjectId("wesplit-bill")
            .setApplicationId("1:548791587175:web:277b0af995feb76dc05fc2")
            .setApiKey("AIzaSyDsaHeM7-_M0utMVZPQNSRsEu5Z5k9BjSw")
            .setStorageBucket("wesplit-bill.appspot.com")
            .build()

    Firebase.initialize(Application(), options)

    return application {
        Window(
            // TODO: Icon
            onCloseRequest = ::exitApplication,
            title = "Wesplit",
        ) {
            window.minimumSize = Dimension(300, 500)
            App(
                module {
                    single<LoginDelegate> { LoginDesktopDelegate() }
                    single<AnalyticsManager> { DesktopAnalyticsManager() }
                    single<ContactListDelegate> { UnsupportedContactListDelegate() }
                },
            )
        }
    }
}
