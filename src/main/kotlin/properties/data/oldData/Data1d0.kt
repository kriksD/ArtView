package properties.data.oldData

import info.ImageInfo
import kotlinx.serialization.Serializable

@Serializable
data class Data1d0(
    val tags: MutableList<String> = mutableListOf(),
    val images: MutableList<ImageInfo> = mutableListOf(),
)
