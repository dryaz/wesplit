import app.wesplit.domain.model.account.LoginDelegate
import app.wesplit.domain.model.account.LoginType
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.auth.externals.GoogleAuthProvider
import dev.gitlive.firebase.auth.externals.signInWithPopup

class LoginJsDelegate : LoginDelegate {
    override fun login(
        type: LoginType,
        onLogin: (Result<FirebaseUser>) -> Unit,
    ) {
        // TODO: Gitlive yet not supported linkWithPopup for anon user, but worth to have it
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
}
