package app.wesplit.utils

import dev.gitlive.firebase.storage.Data
import org.khronos.webgl.Uint8Array

actual fun ByteArray.toPlatformData(): Data = Data(data = Uint8Array(this.toTypedArray()))
