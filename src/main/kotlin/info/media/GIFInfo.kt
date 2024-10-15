package info.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import getImageBitmap
import info.CacheManager
import info.media.serializers.GIFInfoSerializer
import kotlinx.serialization.Serializable
import scaleToMaxValues

@Serializable(with = GIFInfoSerializer::class)
class GIFInfo(
    override val id: Int,
    override val path: String,
    override var name: String = "",
    override var description: String = "",
    override var favorite: Boolean = false,
    override val tags: MutableList<String> = mutableListOf(),
    override var source: String? = null,
    override var rating: String? = null,
    val width: Int,
    val height: Int,
) : MediaInfo {
    override val type: MediaType = MediaType.GIF
    override var thumbnail: ImageBitmap? by mutableStateOf(null)

    override fun loadThumbnail() {
        if (thumbnail != null) return
        val cacheManager = CacheManager()

        val cachedThumbnail = cacheManager.loadThumbnail(id)
        if (cachedThumbnail != null) {
            thumbnail = cachedThumbnail
            return
        }

        val newImage = getImageBitmap(path)

        thumbnail = try {
            newImage?.scaleToMaxValues(400, 400)?.also { cacheManager.cacheThumbnail(it, id) }

        } catch (e: Exception) { newImage }
    }

    override fun thumbnailWeight(): Float = (width.toDouble() / height.toDouble()).toFloat()

    override fun copy(
        id: Int,
        path: String,
        name: String,
        description: String,
        favorite: Boolean,
        tags: MutableList<String>,
        source: String?,
        rating: String?,
    ): MediaInfo {
        return GIFInfo(
            id = id,
            path = path,
            name = name,
            description = description,
            favorite = favorite,
            tags = tags,
            source = source,
            rating = rating,
            width = width,
            height = height,
        )
    }
}