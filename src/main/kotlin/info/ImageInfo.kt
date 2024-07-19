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
import java.awt.Image.SCALE_SMOOTH
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
                newImage.scaleImage(size.first, size.second).also { saveToCache(it) }
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

    private fun ImageBitmap.scaleImage(width: Int, height: Int): ImageBitmap {
        val image = this.toAwtImage().getScaledInstance(width, height, SCALE_SMOOTH)
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d = bufferedImage.createGraphics()
        g2d.drawImage(image, 0, 0, null)
        g2d.dispose()
        return bufferedImage.toComposeImageBitmap()
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
        val newFile = File("${folder.path}${File.separator}${path.substringAfterLast("\\").substringAfterLast("/")}")
        if (newFile.exists()) return

        File(path).copyTo(newFile)
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

