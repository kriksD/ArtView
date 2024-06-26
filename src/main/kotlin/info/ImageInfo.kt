package info

import HasID
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import getImageBitmap
import kotlinx.serialization.Serializable
import openWebpage
import savePngTo
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL

@Serializable(with = ImageInfoSerializer::class)
data class ImageInfo(
    override val id: Int,
    val path: String,
    val width: Int,
    val height: Int,
    var name: String = "",
    var description: String = "",
    var favorite: Boolean = false,
    val tags: MutableList<String> = mutableListOf(),
    var source: String? = null,
    var rating: String? = null,
) : HasID {
    var scaledDownImage: ImageBitmap? by mutableStateOf(null)
    val isLoaded get() = scaledDownImage != null

    fun load() {
        if (scaledDownImage != null) return

        val imageFromCache = loadFromCache()
        if (imageFromCache != null) {
            scaledDownImage = imageFromCache
            return
        }

        val newImage = image

        scaledDownImage = try {
            val newSize = newImage?.let { img -> calculateScaledDownSize(img.width, img.height, 400, 400) }
            newSize?.let { size ->
                newImage.scaleAndCropImage(size.first, size.second).also { saveToCache(it) }
            }

        } catch (e: Exception) { newImage }
    }

    private val cacheDir = File("data/cache/images")

    private fun loadFromCache(): ImageBitmap? {
        cacheDir.mkdirs()

        return if (File(cacheDir, "$id.png").exists()) {
            getImageBitmap(File(cacheDir, "$id.png"))
        } else {
            null
        }
    }

    private fun saveToCache(image: ImageBitmap?) {
        cacheDir.mkdirs()
        image?.savePngTo(File(cacheDir, "$id.png"))
    }

    fun unload() {
        scaledDownImage = null
    }

    private fun ImageBitmap.scaleAndCropImage(width: Int, height: Int): ImageBitmap {
        val bufferedImage = this.toAwtImage()

        val outputImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val scaleX = width.toDouble() / bufferedImage.width.toDouble()
        val scaleY = height.toDouble() / bufferedImage.height.toDouble()
        val scale = kotlin.math.max(scaleX, scaleY)

        val scaledWidth = (bufferedImage.width * scale).toInt()
        val scaledHeight = (bufferedImage.height * scale).toInt()

        val scaledImage = BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB)
        val g2d = scaledImage.createGraphics()
        g2d.drawImage(bufferedImage, 0, 0, scaledWidth, scaledHeight, null)
        g2d.dispose()

        val x = (scaledWidth - width) / 2
        val y = (scaledHeight - height) / 2

        val croppedImage = scaledImage.getSubimage(x, y, width, height)

        val g2dOut = outputImage.createGraphics()
        g2dOut.drawImage(croppedImage, 0, 0, null)
        g2dOut.dispose()

        return outputImage.toComposeImageBitmap()
    }

    private fun calculateScaledDownSize(originalWidth: Int, originalHeight: Int, maxWidth: Int, maxHeight: Int): Pair<Int, Int> {
        val widthRatio = originalWidth.toDouble() / maxWidth.toDouble()
        val heightRatio = originalHeight.toDouble() / maxHeight.toDouble()
        val scaleRatio = maxOf(widthRatio, heightRatio)

        val scaledWidth = (originalWidth.toDouble() / scaleRatio).toInt()
        val scaledHeight = (originalHeight.toDouble() / scaleRatio).toInt()

        return Pair(scaledWidth, scaledHeight)
    }

    val image: ImageBitmap?
        get() {
            val file = File(path)
            if (!file.exists()) return null

            return try {
                val newImage = getImageBitmap(file)
                newImage

            } catch (e: Exception) { null }
        }

    fun saveFileTo(folder: File) {
        File(path).copyTo(
            File("${folder.path}${File.separator}${path.substringAfterLast("\\").substringAfterLast("/")}")
        )
    }

    fun saveFileTo(path: String) = saveFileTo(File(path))

    fun openSource() {
        if (source == null) return
        openWebpage(URL(source))
    }

    fun delete() {
        if (File(path).exists()) File(path).delete()

        val cacheFile = File(cacheDir, "$id.png")
        if (cacheFile.exists()) cacheFile.delete()
    }
}

