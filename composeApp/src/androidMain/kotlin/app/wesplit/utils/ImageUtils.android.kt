package app.wesplit.utils

import dev.gitlive.firebase.storage.Data

actual fun ByteArray.toPlatformData(): Data = Data(this)
