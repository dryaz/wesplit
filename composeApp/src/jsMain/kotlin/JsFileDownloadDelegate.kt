import app.wesplit.FileDownloadDelegate
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

class JsFileDownloadDelegate : FileDownloadDelegate {
    override suspend fun downloadFile(
        fileName: String,
        content: String,
        mimeType: String,
    ): String? {
        try {
            // Create a Blob with the CSV content
            val blob = Blob(arrayOf(content), BlobPropertyBag(type = mimeType))
            val blobUrl = URL.createObjectURL(blob)

            // Trigger browser download
            val anchor = document.createElement("a") as HTMLAnchorElement
            anchor.href = blobUrl
            anchor.download = fileName
            anchor.style.display = "none"
            document.body?.appendChild(anchor)
            anchor.click()
            
            // Clean up after a short delay to ensure download starts
            window.setTimeout({
                document.body?.removeChild(anchor)
                URL.revokeObjectURL(blobUrl)
            }, 100)

            return fileName // Return filename as success indicator
        } catch (e: Exception) {
            console.error("Failed to download file", e)
            return null
        }
    }
}

