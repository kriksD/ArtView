import kotlinx.serialization.Serializable
import properties.Properties
import java.io.File

@Serializable
data class ImageGroup(
    val imagePaths: MutableList<String> = mutableListOf(),
    var name: String = "",
    var description: String = "",
    var favorite: Boolean = false,
    val tags: MutableList<String> = mutableListOf(),
) {

    fun getImageInfo(index: Int): ImageInfo? {
        if (imagePaths.isEmpty()) return null
        return Properties.imagesData().images.find { it.path == imagePaths.getOrNull(index) }
    }

    fun getImageInfoList(): List<ImageInfo> {
        return imagePaths.mapNotNull { path -> Properties.imagesData().images.find { it.path == path } }
    }

    fun saveImageFilesTo(folder: File) {
        imagePaths.forEach {
            File(it).copyTo(
                File("${folder.path}${File.separator}${it.substringAfterLast("\\").substringAfterLast("/")}")
            )
        }
    }
}
