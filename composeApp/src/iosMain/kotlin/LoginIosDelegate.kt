import app.wesplit.domain.model.account.LoginDelegate
import app.wesplit.domain.model.account.LoginType
import dev.gitlive.firebase.auth.FirebaseUser

class LoginIosDelegate : LoginDelegate {
    override fun login(
        type: LoginType,
        onLogin: (Result<FirebaseUser>) -> Unit,
    ) {
        println("Dunno how to login yet :D")
    }
}
