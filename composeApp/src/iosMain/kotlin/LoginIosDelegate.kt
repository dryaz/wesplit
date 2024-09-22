import app.wesplit.domain.model.account.Login
import app.wesplit.domain.model.account.LoginDelegate
import dev.gitlive.firebase.auth.FirebaseUser

class LoginIosDelegate : LoginDelegate {
    override fun socialLogin(
        type: Login.Social.Type,
        onLogin: (Result<FirebaseUser>) -> Unit,
    ) {
        println("Dunno how to login yet :D")
    }
}
