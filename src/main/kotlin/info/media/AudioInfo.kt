package info.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import emptyImageBitmap
import getAudioCover
import info.CacheManager
import info.media.serializers.AudioInfoSerializer
import kotlinx.serialization.Serializable
import mediaData
import scaleToMaxValues
import java.io.File

@Serializable(with = AudioInfoSerializer::class)
class AudioInfo(
    override val id: Int,
    override val path: String,
    override var name: String = "",
    override var description: String = "",
    override var favorite: Boolean = false,
    override val tags: MutableList<String> = mutableListOf(),
    override var source: String? = null,
    override var rating: String? = null,
    val duration: Long,
    var thumbnailID: Int?,
    var thumbnailWidth: Int? = null,
    var thumbnailHeight: Int? = null,
) : MediaInfo {
    override val type: MediaType = MediaType.Audio
    override var thumbnail: ImageBitmap? by mutableStateOf(null)

    override fun loadThumbnail() {
        if (thumbnail != null) return
        val cacheManager = CacheManager()

        val cachedThumbnail = cacheManager.loadThumbnail(id)
        if (cachedThumbnail != null) {
            thumbnail = cachedThumbnail
            return
        }

        val newImage = cover

        thumbnail = try {
            newImage?.scaleToMaxValues(400, 400)?.also { cacheManager.cacheThumbnail(it, id) }
                ?: emptyImageBitmap

        } catch (e: Exception) { newImage ?: emptyImageBitmap }
    }

    override fun thumbnailWeight(): Float {
        val width = thumbnailWidth?.toDouble() ?: return 1.0F
        val height = thumbnailHeight?.toDouble() ?: return 1.0F
        return (width / height).toFloat()
    }

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
        return AudioInfo(
            id = id,
            path = path,
            name = name,
            description = description,
            favorite = favorite,
            tags = tags,
            source = source,
            rating = rating,
            duration = duration,
            thumbnailID = thumbnailID,
            thumbnailWidth = thumbnailWidth,
            thumbnailHeight = thumbnailHeight,
        )
    }

    fun setCover(mediaID: Int) {
        val media = mediaData.findMedia(mediaID) ?: return
        val image = if (media is ImageInfo) media.image else return

        thumbnailID = mediaID
        thumbnailWidth = image?.width
        thumbnailHeight = image?.height

        CacheManager().deleteThumbnail(id)
    }

    fun removeCover() {
        thumbnailID = null
        thumbnailWidth = null
        thumbnailHeight = null

        getAudioCover(path)?.let {
            thumbnailWidth = it.width
            thumbnailHeight = it.height
        }

        CacheManager().deleteThumbnail(id)
    }

    val cover: ImageBitmap?
        get() {
            val file = File(path)
            if (!file.exists()) return null

            return try {
                val newImage = thumbnailID?.let { id ->
                    val media = mediaData.findMedia(id)
                    if (media is ImageInfo) media.image else null

                } ?: getAudioCover(path)

                newImage

            } catch (e: Exception) { null }
        }
}