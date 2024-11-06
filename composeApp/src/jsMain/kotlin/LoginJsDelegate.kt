import app.wesplit.domain.model.account.Login
import app.wesplit.domain.model.account.LoginDelegate
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.auth.externals.GoogleAuthProvider
import dev.gitlive.firebase.auth.externals.OAuthProvider
import dev.gitlive.firebase.auth.externals.signInWithPopup
import dev.gitlive.firebase.auth.js

class LoginJsDelegate : LoginDelegate {
    override fun socialLogin(
        type: Login.Social.Type,
        onLogin: (Result<FirebaseUser>) -> Unit,
    ) {
        when (type) {
            Login.Social.Type.GOOGLE -> signWithGoogle(onLogin)
            Login.Social.Type.APPLE -> signWithApple(onLogin)
        }
        // TODO: Gitlive yet not supported linkWithPopup for anon user, but worth to have it
    }

    private fun signWithGoogle(onLogin: (Result<FirebaseUser>) -> Unit) {
        signInWithPopup(Firebase.auth.js, GoogleAuthProvider()).then { result ->
            val user = Firebase.auth.currentUser
            val googleJsCreds = GoogleAuthProvider.credentialFromResult(result)
            // Here we can't link acc 'cause acc already created, firebase could only link new non created acc
            // There is linkWithPopup API in firebase JS but not yet implemented in Gitlibe
            // TODO: PR to opensource gitlive to support link with
            //            val googleCreds = dev.gitlive.firebase.auth.GoogleAuthProvider.credential(
            //                idToken = googleJsCreds?.idToken,
            //                accessToken = googleJsCreds?.accessToken
            //            )
            if (user != null) {
                onLogin(Result.success(user))
            } else {
                throw NullPointerException("Firebase user is null")
            }
        }.catch { exception ->
            onLogin(Result.failure(exception))
        }
    }

    private fun signWithApple(onLogin: (Result<FirebaseUser>) -> Unit) {
        val authProvider = OAuthProvider("apple.com")
        authProvider.addScope("email")
        authProvider.addScope("name")

        signInWithPopup(Firebase.auth.js, authProvider).then { result ->
            val user = Firebase.auth.currentUser
            val googleJsCreds = GoogleAuthProvider.credentialFromResult(result)
            // Here we can't link acc 'cause acc already created, firebase could only link new non created acc
            // There is linkWithPopup API in firebase JS but not yet implemented in Gitlibe
            // TODO: PR to opensource gitlive to support link with
            //            val googleCreds = dev.gitlive.firebase.auth.GoogleAuthProvider.credential(
            //                idToken = googleJsCreds?.idToken,
            //                accessToken = googleJsCreds?.accessToken
            //            )
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
