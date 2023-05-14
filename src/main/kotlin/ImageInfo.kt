import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File

@Serializable
data class ImageInfo(
    val path: String,
    var name: String = "",
    var description: String = "",
    var favorite: Boolean = false,
    val tags: MutableList<String> = mutableListOf(),
) {
    @Transient
    var image: ImageBitmap? = null
        get() {
            if (field != null) return field

            val file = File(path)
            if (!file.exists()) return null

            return try {
                val newImage = getImageBitmap(file)
                image = newImage
                newImage

            } catch (e: Exception) { null }
        }

    fun delete() { File(path).delete() }
}