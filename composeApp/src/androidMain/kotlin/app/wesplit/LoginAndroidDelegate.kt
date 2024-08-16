package app.wesplit

import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import app.wesplit.di.ActivityProvider
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

// TODO https://developer.android.com/identity/sign-in/credential-manager-siwg
@Single
class LoginAndroidDelegate(
    private val activityProvider: ActivityProvider,
) : LoginDelegate {
    override fun login(type: LoginType, onLogin: (Result<FirebaseUser>) -> Unit) {
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(
            "548791587175-k0hhfmjvk6utruqmq7d5d9but53ods4n.apps.googleusercontent.com"
        ).setNonce("wtf dunno yet").build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()
        val activity = activityProvider.activeActivity ?: throw IllegalStateException("Can't find acitvity")

        val credentialManager = CredentialManager.create(activityProvider.activeActivity ?: throw IllegalStateException("Can't find acitvity"))
        GlobalScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = activity,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                e.printStackTrace()
            }
        }

    }

    fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential
        Log.e("!@#", "$credential")

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("!@#", "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized credential type here.
                    Log.e("!@#", "Unexpected type of credential")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e("!@#", "Unexpected type of credential")
            }
        }
    }
}
