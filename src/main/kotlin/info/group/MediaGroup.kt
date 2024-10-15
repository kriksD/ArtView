package info.group

import utilities.HasID
import info.media.MediaInfo
import kotlinx.serialization.Serializable
import mediaData
import java.io.File

@Serializable(with = MediaGroupSerializer::class)
data class MediaGroup(
    override val id: Int,
    val mediaIDs: MutableList<Int> = mutableListOf(),
    var name: String = "",
    var description: String = "",
    var favorite: Boolean = false,
    val tags: MutableList<String> = mutableListOf(),
) : HasID {

    fun getMediaInfo(index: Int): MediaInfo? {
        if (mediaIDs.isEmpty()) return null
        mediaIDs.getOrNull(index)?.let { return mediaData.findMedia(it) }
        return null
    }

    fun getImageMediaList(): List<MediaInfo> {
        return mediaIDs.mapNotNull { mediaData.findMedia(it) }
    }

    fun saveImageFilesTo(folder: File) {
        mediaIDs.forEach {
            val mediaInfo = mediaData.findMedia(it) ?: return
            val newFile = folder.resolve(mediaInfo.name)
            if (newFile.exists()) return

            File(mediaInfo.path).copyTo(newFile)
        }
    }
}
