package app.wesplit

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App(module {
                single<LoginDelegate> { LoginAndroidDelegate(get()) }
            })
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
