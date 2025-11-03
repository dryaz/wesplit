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
            val url = URL.createObjectURL(blob)

            // Try to use Web Share API if available
            if (js("navigator.share") != null && js("navigator.canShare") != null) {
                try {
                    val file = js("new File([blob], fileName, { type: mimeType })")
                    val shareData = js("{ files: [file], title: fileName }")
                    
                    // Check if can share files
                    val canShare = js("navigator.canShare(shareData)")
                    if (canShare as? Boolean == true) {
                        js("navigator.share(shareData)")
                        URL.revokeObjectURL(url)
                        return url
                    }
                } catch (e: Exception) {
                    console.log("Web Share API failed, falling back to download", e)
                }
            }

            // Fallback: trigger download
            val anchor = document.createElement("a") as HTMLAnchorElement
            anchor.href = url
            anchor.download = fileName
            document.body?.appendChild(anchor)
            anchor.click()
            document.body?.removeChild(anchor)

            // Clean up the URL object
            URL.revokeObjectURL(url)

            return url
        } catch (e: Exception) {
            console.error("Failed to download file", e)
            return null
        }
    }
}

