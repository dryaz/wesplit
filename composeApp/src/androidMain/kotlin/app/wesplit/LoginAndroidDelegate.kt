package app.wesplit

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import app.wesplit.di.ActivityProvider
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.account.LoginDelegate
import app.wesplit.domain.model.account.LoginType
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

// https://developer.android.com/identity/sign-in/credential-manager-siwg
// TODO: Add SHA1 for debug/release certs to enable google sign in in android
@Single
class LoginAndroidDelegate(
    private val activityProvider: ActivityProvider,
    private val analyticsManager: AnalyticsManager,
) : LoginDelegate {
    override fun login(
        type: LoginType,
        onLogin: (Result<FirebaseUser>) -> Unit,
    ) {
        val signInWithGoogleOption: GetSignInWithGoogleOption =
            GetSignInWithGoogleOption.Builder(
                "548791587175-k0hhfmjvk6utruqmq7d5d9but53ods4n.apps.googleusercontent.com",
            ).setNonce("wtf dunno yet").build()

        val request: GetCredentialRequest =
            GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()
        val activity = activityProvider.activeActivity ?: throw IllegalStateException("Can't find acitvity")

        val credentialManager =
            CredentialManager.create(
                activityProvider.activeActivity ?: throw IllegalStateException("Can't find acitvity"),
            )
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result =
                    credentialManager.getCredential(
                        request = request,
                        context = activity,
                    )
                onLogin(handleSignIn(result))
            } catch (e: GetCredentialException) {
                analyticsManager.log(e)
                onLogin(Result.failure(e))
            }
        }
    }

    suspend fun handleSignIn(result: GetCredentialResponse): Result<FirebaseUser> {
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val authCredential = GoogleAuthProvider.credential(googleIdTokenCredential.idToken, null)

                        val signinResult =
                            kotlin.runCatching {
                                Firebase.auth.currentUser?.let {
                                    Firebase.auth.currentUser?.linkWithCredential(authCredential)
                                } ?: Firebase.auth.signInWithCredential(authCredential)
                            }.onFailure { e ->
                                analyticsManager.log(e)
                            }.recover {
                                Firebase.auth.signInWithCredential(authCredential)
                            }.getOrNull()

                        val user = signinResult?.user
                        if (user != null) {
                            return Result.success(user)
                        } else {
                            return Result.failure(IllegalAccessException("Can't get firebase user"))
                        }
                        // TODO: Continue with firebase
                        //  https://firebase.google.com/docs/auth/android/google-signin
                    } catch (e: GoogleIdTokenParsingException) {
                        analyticsManager.log(e)
                    }
                } else {
                    // Catch any unrecognized credential type here.
                    analyticsManager.log(IllegalArgumentException("Unexpected type of credential"))
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                analyticsManager.log(IllegalArgumentException("Unexpected type of credential"))
            }
        }

        return Result.failure(IllegalAccessException("Can't get firebase user"))
    }
}
