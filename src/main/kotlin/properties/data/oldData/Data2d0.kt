package properties.data.oldData

import ImageGroup
import TagCategory
import kotlinx.serialization.Serializable

@Serializable
data class Data2d0(
    val tags: MutableList<TagCategory> = mutableListOf(TagCategory("Other", mutableListOf("NSFW"))),
    val images: MutableList<ImageInfo2d0> = mutableListOf(),
    val imageGroups: MutableList<ImageGroup> = mutableListOf(),
)