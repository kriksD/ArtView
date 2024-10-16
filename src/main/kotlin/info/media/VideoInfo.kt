package info.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import getVideoFrame
import getVideoFrameCount
import info.CacheManager
import info.media.serializers.VideoInfoSerializer
import kotlinx.serialization.Serializable
import mediaData
import scaleToMaxValues

@Serializable(with = VideoInfoSerializer::class)
class VideoInfo(
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
    val duration: Long,
    var thumbnailFrame: Int = 0,
    var thumbnailID: Int? = null,
    var thumbnailWidth: Int? = null,
    var thumbnailHeight: Int? = null,
) : MediaInfo {
    override val type: MediaType = MediaType.Video
    override var thumbnail: ImageBitmap? by mutableStateOf(null)

    override fun loadThumbnail() {
        if (thumbnail != null) return
        val cacheManager = CacheManager()

        val cachedThumbnail = cacheManager.loadThumbnail(id)
        if (cachedThumbnail != null) {
            thumbnail = cachedThumbnail
            return
        }

        val newImage = cover ?: getVideoFrame(path, thumbnailFrame)

        thumbnail = try {
            newImage?.scaleToMaxValues(400, 400)?.also { cacheManager.cacheThumbnail(it, id) }

        } catch (e: Exception) { newImage }
    }

    override fun thumbnailWeight(): Float {
        if (thumbnailID != null && thumbnailWidth != null && thumbnailHeight != null) {
            val tWidth = thumbnailWidth?.toDouble() ?: return (width.toDouble() / height.toDouble()).toFloat()
            val tHeight = thumbnailHeight?.toDouble() ?: return (width.toDouble() / height.toDouble()).toFloat()
            return (tWidth / tHeight).toFloat()
        }

        return (width.toDouble() / height.toDouble()).toFloat()
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
        return VideoInfo(
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
            duration = duration,
            thumbnailID = thumbnailID,
            thumbnailWidth = thumbnailWidth,
            thumbnailHeight = thumbnailHeight,
        )
    }

    fun setFrameAsThumbnail(frame: Int) {
        if (frame < getVideoFrameCount(path)) {
            thumbnailFrame = frame

            CacheManager().deleteThumbnail(id)
        }
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

        CacheManager().deleteThumbnail(id)
    }

    val cover: ImageBitmap?
        get() {
            return try {
                val newImage = thumbnailID?.let { id ->
                    val media = mediaData.findMedia(id)
                    if (media is ImageInfo) media.image else null
                } ?: getVideoFrame(path, thumbnailFrame)

                newImage

            } catch (e: Exception) { null }
        }
}