import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import app.wesplit.data.firebase.domainModule
import app.wesplit.data.firebase.firebaseDataModule
import app.wesplit.domain.model.account.AccountRepository
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(domainModule() + firebaseDataModule())
    }) {
        MaterialTheme {
            var showContent by remember { mutableStateOf(false) }
            val userRepo = koinInject<AccountRepository>()

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = { showContent = !showContent }) {
                    Text("Click me!")
                }
                AnimatedVisibility(showContent) {
                    val greeting = remember { "Hey" }
                    Column(
                        Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(painterResource(Res.drawable.compose_multiplatform), null)
                        Text("Compose: ${userRepo.get()}")
                    }
                }
            }
        }
    }
}