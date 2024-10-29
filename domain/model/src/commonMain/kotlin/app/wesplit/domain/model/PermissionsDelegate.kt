package app.wesplit

interface PermissionsDelegate {
    suspend fun requestPermission(permission: Permission): Result<Unit>
}

enum class Permission {
    PUSH,
}

object DefaultPermissionDelegate : PermissionsDelegate {
    override suspend fun requestPermission(permission: Permission): Result<Unit> {
        return Result.success(Unit)
    }
}
