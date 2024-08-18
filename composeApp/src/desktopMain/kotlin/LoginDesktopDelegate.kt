import app.wesplit.LoginDelegate
import app.wesplit.LoginType
import dev.gitlive.firebase.auth.FirebaseUser

class LoginDesktopDelegate : LoginDelegate {
    override fun login(
        type: LoginType,
        onLogin: (Result<FirebaseUser>) -> Unit,
    ) {
        println("Dunno how to login yet :D")
    }
}
