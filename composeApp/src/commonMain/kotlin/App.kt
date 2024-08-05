import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.wesplit.data.firebase.domainModule
import app.wesplit.data.firebase.firebaseDataModule
import app.wesplit.domain.model.account.AccountRepository
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.util.DebugLogger
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(domainModule() + firebaseDataModule())
    }) {
        MaterialTheme {
            var showContent by remember { mutableStateOf(true) }
            val userRepo = koinInject<AccountRepository>()

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = { showContent = !showContent }) {
                    Text("Click me!")
                }

                val imageLoader = SingletonImageLoader
                    .get(LocalPlatformContext.current)
                    .newBuilder()
                    .logger(DebugLogger())
                    .build()

                AsyncImage(
                    modifier = Modifier.size(400.dp),
                    imageLoader = imageLoader,
                    model = "https://fastly.picsum.photos/id/704/200/300.jpg?hmac=L0hDmSHSy2cciN8xRC5-EgnjSOLcurqToggugp9Deng",
                    contentDescription = null
                )

                AnimatedVisibility(showContent) {
                    val greeting = remember { "Hey" }
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Compose: ${userRepo.get()}")
                    }
                }
            }
        }
    }
}
