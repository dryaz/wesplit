import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.auth.externals.GoogleAuthProvider
import dev.gitlive.firebase.auth.externals.signInWithPopup

class LoginJsDelegate : LoginDelegate {
    override fun login(type: LoginType, onLogin: (Result<FirebaseUser>) -> Unit) {
        signInWithPopup(Firebase.auth.js, GoogleAuthProvider()).then { result ->
            val user = Firebase.auth.currentUser
            if (user != null) {
                onLogin(Result.success(user))
            } else {
                throw NullPointerException("Firebase user is null")
            }
        }.catch { exception ->
            onLogin(Result.failure(exception))
        }
    }
}
