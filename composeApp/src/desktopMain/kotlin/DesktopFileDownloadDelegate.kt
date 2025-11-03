import app.wesplit.FileDownloadDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

class DesktopFileDownloadDelegate : FileDownloadDelegate {
    override suspend fun downloadFile(
        fileName: String,
        content: String,
        mimeType: String,
    ): String? =
        withContext(Dispatchers.IO) {
            try {
                // Show save dialog
                val fileDialog =
                    FileDialog(null as Frame?, "Save CSV", FileDialog.SAVE).apply {
                        file = fileName
                        isVisible = true
                    }

                val selectedFile = fileDialog.file
                val selectedDirectory = fileDialog.directory

                if (selectedFile != null && selectedDirectory != null) {
                    val file = File(selectedDirectory, selectedFile)
                    file.writeText(content)
                    
                    // Open the file with default application (could be used for sharing)
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(file)
                        }
                    } catch (e: Exception) {
                        // Ignore if can't open
                    }
                    
                    file.absolutePath
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}

