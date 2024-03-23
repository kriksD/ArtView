package properties.data.oldData

import kotlinx.serialization.Serializable

@Serializable
data class ImageInfo2d0(
    val path: String,
    var name: String = "",
    var description: String = "",
    var favorite: Boolean = false,
    val tags: MutableList<String> = mutableListOf(),
)