import app.wesplit.domain.model.account.Login
import app.wesplit.domain.model.account.LoginDelegate
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginIosDelegate(
    private val loginIosNativeDelegate: LoginIosNativeDelegate,
) : LoginDelegate {
    override fun socialLogin(
        type: Login.Social.Type,
        onLogin: (Result<FirebaseUser>) -> Unit,
    ) {
        if (type == Login.Social.Type.GOOGLE) {
            loginIosNativeDelegate.googleLogin { creds ->
                if (creds != null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val result =
                            Firebase.auth.signInWithCredential(
                                GoogleAuthProvider.credential(
                                    accessToken = creds.accessToken,
                                    idToken = creds.idToken,
                                ),
                            )
                        val user = result.user
                        if (user != null) {
                            onLogin(Result.success(user))
                        } else {
                            onLogin(Result.failure(NullPointerException("Error while login")))
                        }
                    }
                } else {
                    onLogin(Result.failure(NullPointerException("Error while login")))
                }
            }
        }
    }
}

interface LoginIosNativeDelegate {
    fun googleLogin(onCredentialsReceived: (GooleCredentials?) -> Unit)
}

data class GooleCredentials(
    val accessToken: String,
    val idToken: String,
)
