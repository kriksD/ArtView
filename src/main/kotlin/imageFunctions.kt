import androidx.compose.ui.graphics.ImageBitmap
import properties.Properties
import java.io.File

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
        Properties.imagesData().images.add(0, it)
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
        Properties.imagesData().images.add(0, it)
    }
}