package app.wesplit

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import app.wesplit.di.ActivityProvider
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel
import app.wesplit.domain.model.account.Login
import app.wesplit.domain.model.account.LoginDelegate
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.OAuthProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.android
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
    override fun socialLogin(
        type: Login.Social.Type,
        onLogin: (Result<FirebaseUser>) -> Unit,
    ) {
        when (type) {
            Login.Social.Type.GOOGLE -> signInWithGoogle(onLogin)
            Login.Social.Type.APPLE -> signInWithApple(onLogin)
        }
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
                analyticsManager.log(e)
                onLogin(Result.failure(e))
            }
        }
    }

    private fun signInWithApple(onLogin: (Result<FirebaseUser>) -> Unit) {
        val provider = OAuthProvider.newBuilder("apple.com")
        provider.setScopes(listOf("email", "name"))

        val pending = Firebase.auth.android.pendingAuthResult
        if (pending != null) {
            pending.addOnSuccessListener { authResult ->
                analyticsManager.log("checkPending:onSuccess:$authResult", LogLevel.WARNING)
                val user = Firebase.auth.currentUser
                if (user != null) {
                    onLogin(Result.success(user))
                } else {
                    val e = IllegalStateException("Login success but user is null")
                    analyticsManager.log(e)
                    onLogin(Result.failure(e))
                }
            }.addOnFailureListener { e ->
                analyticsManager.log(e)
            }
        } else {
            activityProvider.activeActivity?.let {
                Firebase.auth.android.startActivityForSignInWithProvider(it, provider.build())
                    .addOnSuccessListener { authResult ->
                        // Sign-in successful!
                        analyticsManager.log("Login with Apple on Android", LogLevel.WARNING)
                        val user = Firebase.auth.currentUser
                        if (user != null) {
                            onLogin(Result.success(user))
                        } else {
                            val e = IllegalStateException("Login success but user is null")
                            analyticsManager.log(e)
                            onLogin(Result.failure(e))
                        }
                    }
                    .addOnFailureListener { e ->
                        analyticsManager.log(e)
                    }
            }
        }
    }

    private fun signInWithGoogle(onLogin: (Result<FirebaseUser>) -> Unit) {
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
                    } catch (e: Throwable) {
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
