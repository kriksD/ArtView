package info.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import getImageBitmap
import info.CacheManager
import info.media.serializers.ImageInfoSerializer
import kotlinx.serialization.Serializable
import scaleToMaxValues
import java.io.File

@Serializable(with = ImageInfoSerializer::class)
class ImageInfo(
    override val id: Int,
    override val path: String,
    override var name: String = "",
    override var description: String = "",
    override var favorite: Boolean = false,
    override var hidden: Boolean = false,
    override val tags: MutableList<String> = mutableListOf(),
    override var source: String? = null,
    override var rating: String? = null,
    val width: Int,
    val height: Int,
) : MediaInfo {
    override val type: MediaType = MediaType.Image
    override var thumbnail: ImageBitmap? by mutableStateOf(null)

    override fun loadThumbnail() {
        if (thumbnail != null) return
        val cacheManager = CacheManager()

        val cachedThumbnail = cacheManager.loadThumbnail(id)
        if (cachedThumbnail != null) {
            thumbnail = cachedThumbnail
            return
        }

        val newImage = image

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
        hidden: Boolean,
        tags: MutableList<String>,
        source: String?,
        rating: String?,
    ): MediaInfo {
        return ImageInfo(
            id = id,
            path = path,
            name = name,
            description = description,
            favorite = favorite,
            hidden = hidden,
            tags = tags,
            source = source,
            rating = rating,
            width = width,
            height = height,
        )
    }

    fun copy(
        id: Int = this.id,
        path: String = this.path,
        name: String = this.name,
        description: String = this.description,
        favorite: Boolean = this.favorite,
        hidden: Boolean = this.hidden,
        tags: MutableList<String> = this.tags,
        source: String? = this.source,
        rating: String? = this.rating,
        width: Int = this.width,
        height: Int = this.height,
    ): ImageInfo {
        return ImageInfo(
            id = id,
            path = path,
            name = name,
            description = description,
            favorite = favorite,
            hidden = hidden,
            tags = tags,
            source = source,
            rating = rating,
            width = width,
            height = height,
        )
    }

    val image: ImageBitmap?
        get() {
            val file = File(path)
            if (!file.exists()) return null

            return try {
                val newImage = getImageBitmap(file)
                newImage

            } catch (e: Exception) { null }
        }
}

