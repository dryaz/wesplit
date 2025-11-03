import app.wesplit.FileDownloadDelegate
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

class IosFileDownloadDelegate : FileDownloadDelegate {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun downloadFile(
        fileName: String,
        content: String,
        mimeType: String,
    ): String? =
        withContext(Dispatchers.IO) {
            try {
                // Get Documents directory
                val paths =
                    NSSearchPathForDirectoriesInDomains(
                        NSDocumentDirectory,
                        NSUserDomainMask,
                        true,
                    )
                val documentsDirectory = paths.firstOrNull() as? String ?: return@withContext null
                val filePath = "$documentsDirectory/$fileName"

                // Write file
                val nsString = NSString.create(string = content)
                val data = nsString.dataUsingEncoding(NSUTF8StringEncoding) ?: return@withContext null
                val success = data.writeToFile(filePath, atomically = true)

                if (success) {
                    // Show share sheet to let user share/save the file
                    withContext(Dispatchers.Main) {
                        val fileUrl = platform.Foundation.NSURL.fileURLWithPath(filePath)
                        val activityViewController =
                            UIActivityViewController(
                                activityItems = listOf(fileUrl),
                                applicationActivities = null,
                            )

                        UIApplication.topViewController()?.presentViewController(
                            activityViewController,
                            animated = true,
                            completion = null,
                        )
                    }
                    filePath
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}

