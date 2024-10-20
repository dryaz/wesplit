package app.wesplit.utils

import dev.gitlive.firebase.storage.Data
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.resized
import korlibs.image.format.ImageDecodingProps
import korlibs.image.format.ImageEncodingProps
import korlibs.image.format.ImageFormats
import korlibs.image.format.JPEGInfo
import korlibs.image.format.PNG
import korlibs.io.stream.openAsync
import korlibs.math.geom.Anchor
import korlibs.math.geom.ScaleMode

expect fun ByteArray.toPlatformData(): Data

suspend fun ByteArray.resizeImage(
    fileName: String,
    desiredWidth: Int,
    desiredHeight: Int,
): ByteArray {
    // Decode the image from ByteArray
    val imageFormats = ImageFormats(PNG, JPEGInfo)
    val originalImage: Bitmap = imageFormats.decode(openAsync(), ImageDecodingProps(fileName))

    // Calculate the new dimensions while maintaining aspect ratio
    val (newWidth, newHeight) =
        calculateNewDimensions(
            originalWidth = originalImage.width,
            originalHeight = originalImage.height,
            desiredWidth = desiredWidth,
            desiredHeight = desiredHeight,
        )

    // Resize the image
    val resizedImage: Bitmap = originalImage.resized(newWidth, newHeight, ScaleMode.COVER, Anchor.CENTER)

    // Encode the resized image into the desired format
    return imageFormats.encode(resizedImage, ImageEncodingProps(fileName))
}

/**
 * Calculates new dimensions to maintain aspect ratio.
 */
private fun calculateNewDimensions(
    originalWidth: Int,
    originalHeight: Int,
    desiredWidth: Int,
    desiredHeight: Int,
): Pair<Int, Int> {
    val widthRatio = desiredWidth.toDouble() / originalWidth
    val heightRatio = desiredHeight.toDouble() / originalHeight
    val scaleFactor = minOf(widthRatio, heightRatio)

    val newWidth = (originalWidth * scaleFactor).toInt()
    val newHeight = (originalHeight * scaleFactor).toInt()

    return Pair(newWidth, newHeight)
}
