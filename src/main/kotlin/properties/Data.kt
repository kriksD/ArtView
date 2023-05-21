package properties

import ImageInfo
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.serialization.Serializable
import saveWebPTo
import uniqueName
import java.io.File

@Serializable
data class Data(
    val tags: MutableList<String> = mutableListOf(),
    val images: MutableList<ImageInfo> = mutableListOf(),
) {
    fun addImage(file: File): ImageInfo? {
        if (!file.exists()) return null

        val newFile = File("images/${
            uniqueName(file.nameWithoutExtension, file.extension, File("images"))
        }.${file.extension}")

        file.copyTo(newFile)

        return ImageInfo(
            newFile.path,
            newFile.name,
        ).also {
            images.add(0, it)
        }
    }

    fun addImage(path: String): ImageInfo? = addImage(File(path))

    fun addImage(image: ImageBitmap): ImageInfo {
        val newFile = File("images/${uniqueName("new_image", ".png", File("images"))}.png")
        image.saveWebPTo(newFile)

        return ImageInfo(
            newFile.path,
            newFile.name,
        ).also {
            images.add(0, it)
        }
    }

    fun delete(imageInfo: ImageInfo) {
        val found = images.find { it.path == imageInfo.path }
        found?.let {
            images.remove(it)
            it.delete()
        }
    }
}