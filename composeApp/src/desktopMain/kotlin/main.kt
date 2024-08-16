import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.wesplit.App
import java.awt.Dimension

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Split",
        ) {
            window.minimumSize = Dimension(300, 500)
            App()
        }
    }
