package properties

import ImageInfo
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    val tags: MutableList<String> = mutableListOf(),
    val images: MutableList<ImageInfo> = mutableListOf(),
) {
    fun delete(imageInfo: ImageInfo) {
        val found = images.find { it.path == imageInfo.path }
        found?.let {
            images.remove(it)
            it.delete()
        }
    }
}