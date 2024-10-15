package info.group

import utilities.HasID
import info.media.MediaInfo
import kotlinx.serialization.Serializable
import mediaData
import java.io.File

@Serializable(with = MediaGroupSerializer::class)
data class MediaGroup(
    override val id: Int,
    val paths: MutableList<String> = mutableListOf(),
    var name: String = "",
    var description: String = "",
    var favorite: Boolean = false,
    val tags: MutableList<String> = mutableListOf(),
) : HasID {

    fun getMediaInfo(index: Int): MediaInfo? {
        if (paths.isEmpty()) return null
        return mediaData.mediaList.find { it.path == paths.getOrNull(index) }
    }

    fun getImageInfoList(): List<MediaInfo> {
        return paths.mapNotNull { path -> mediaData.mediaList.find { it.path == path } }
    }

    fun saveImageFilesTo(folder: File) {
        paths.forEach {
            val newFile = File("${folder.path}${File.separator}${it.substringAfterLast("\\").substringAfterLast("/")}")
            if (newFile.exists()) return

            File(it).copyTo(newFile)
        }
    }
}
