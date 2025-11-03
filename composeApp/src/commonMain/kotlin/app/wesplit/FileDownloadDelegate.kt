package app.wesplit

interface FileDownloadDelegate {
    /**
     * Downloads/saves a file with the given content to the device.
     * 
     * @param fileName Name of the file to save
     * @param content Content of the file
     * @param mimeType MIME type of the file (e.g., "text/csv")
     * @return File path/URI if successful, null otherwise
     */
    suspend fun downloadFile(
        fileName: String,
        content: String,
        mimeType: String,
    ): String?
}

object DefaultFileDownloadDelegate : FileDownloadDelegate {
    override suspend fun downloadFile(
        fileName: String,
        content: String,
        mimeType: String,
    ): String? {
        // Default implementation - just log
        println("FileDownload: $fileName")
        return null
    }
}

