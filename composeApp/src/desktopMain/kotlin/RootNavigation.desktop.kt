import dev.gitlive.firebase.auth.FirebaseUser

actual fun login(
    type: LoginType,
    onLogin: (Result<FirebaseUser>) -> Unit,
) {
}
