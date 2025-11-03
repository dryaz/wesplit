import app.wesplit.domain.model.account.Login
import app.wesplit.domain.model.account.LoginDelegate
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginDesktopDelegate : LoginDelegate {
    override fun socialLogin(
        type: Login.Social.Type,
        onLogin: (Result<FirebaseUser>) -> Unit,
    ) {
        println("Dunno how to login yet :D")
    }

    override fun anonymousLogin(onLogin: (Result<FirebaseUser>) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = Firebase.auth.signInAnonymously()
                val user = result.user
                if (user != null) {
                    onLogin(Result.success(user))
                } else {
                    onLogin(Result.failure(NullPointerException("Anonymous login failed")))
                }
            } catch (e: Exception) {
                onLogin(Result.failure(e))
            }
        }
    }
}
