package properties.data

import ImageInfo
import kotlinx.serialization.Serializable

@Serializable
data class OldData(
    val tags: MutableList<String> = mutableListOf(),
    val images: MutableList<ImageInfo> = mutableListOf(),
)
