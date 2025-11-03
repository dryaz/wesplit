package app.wesplit

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AndroidFileDownloadDelegate(
    private val context: Context,
) : FileDownloadDelegate {
    override suspend fun downloadFile(
        fileName: String,
        content: String,
        mimeType: String,
    ): String? =
        withContext(Dispatchers.IO) {
            try {
                val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Use MediaStore for Android 10+
                    val contentValues =
                        ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }

                    val resolver = context.contentResolver
                    val insertedUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                    insertedUri?.let {
                        resolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write(content.toByteArray())
                        }
                        it
                    }
                } else {
                    // For older Android versions - use app cache for sharing
                    val cacheDir = File(context.cacheDir, "csv_exports")
                    cacheDir.mkdirs()
                    val file = File(cacheDir, fileName)
                    FileOutputStream(file).use { outputStream ->
                        outputStream.write(content.toByteArray())
                    }
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                }

                uri?.let {
                    // Show share dialog
                    withContext(Dispatchers.Main) {
                        val shareIntent =
                            Intent(Intent.ACTION_SEND).apply {
                                type = mimeType
                                putExtra(Intent.EXTRA_STREAM, it)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        context.startActivity(Intent.createChooser(shareIntent, "Share CSV").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    it.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}

