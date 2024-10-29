package app.wesplit

import dev.icerock.moko.permissions.PermissionsController
import org.koin.core.annotation.Single

@Single
class AndroidPermissionDelegate(
    private val permissionsController: PermissionsController,
) : PermissionsDelegate {
    override suspend fun requestPermission(permission: Permission): Result<Unit> =
        kotlin.runCatching {
            permissionsController.providePermission(
                when (permission) {
                    Permission.PUSH -> dev.icerock.moko.permissions.Permission.REMOTE_NOTIFICATION
                },
            )
            return Result.success(Unit)
        }
}
