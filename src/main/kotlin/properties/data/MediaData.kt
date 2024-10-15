package properties.data

import info.group.MediaGroup
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import info.media.*
import kotlinx.serialization.Serializable
import properties.DataFolder
import properties.Properties
import savePngTo
import toFileName
import utilities.uniqueId
import uniqueName
import java.io.File

@Serializable(with = MediaDataSerializer::class)
class MediaData(
    var dataVersion: String = Properties.DATA_VERSION,
    mediaList: Collection<MediaInfo> = mutableListOf(),
    mediaGroups: Collection<MediaGroup> = mutableListOf(),
) {
    val mediaList = mediaList.toMutableStateList()
    val mediaGroups = mediaGroups.toMutableStateList()

    fun addMedia(file: File, createCopy: Boolean = true): MediaInfo? {
        val mediaInfo = MediaInfoFactory.makeFromFile(file, mediaList, createCopy) ?: return null
        mediaList.add(0, mediaInfo)
        return mediaInfo

        /*if (!file.exists()) return null

        if (file.extension == "gif") {
            val newName = uniqueName(file.nameWithoutExtension, file.extension, DataFolder.gifFolder)
            val newFile = DataFolder.gifFolder.resolve("$newName.${file.extension}")
            file.copyTo(newFile)

            val image = getImageBitmap(newFile) ?: return null

            return GIFInfo(
                id = mediaList.uniqueId(),
                path = newFile.path,
                name = newFile.name,
                width = image.width,
                height = image.height,
            ).also {
                mediaList.add(0, it)
            }
        }

        getImageBitmap(file)?.let { image ->
            val newName = uniqueName(file.nameWithoutExtension, file.extension, DataFolder.imageFolder)
            val newFile = DataFolder.imageFolder.resolve("$newName.${file.extension}")
            file.copyTo(newFile)

            return ImageInfo(
                id = mediaList.uniqueId(),
                path = newFile.path,
                name = newFile.name,
                width = image.width,
                height = image.height,
            ).also {
                mediaList.add(0, it)
            }
        }

        if (isVideoFileSupported(file)) {
            val newName = uniqueName(file.nameWithoutExtension, file.extension, DataFolder.videoFolder)
            val newFile = DataFolder.videoFolder.resolve("$newName.${file.extension}")
            file.copyTo(newFile)

            val dimensions = getVideoDimensions(file) ?: return null

            return VideoInfo(
                id = mediaList.uniqueId(),
                path = newFile.path,
                name = newFile.name,
                width = dimensions.width,
                height = dimensions.height,
                duration = getVideoDuration(file) ?: return null,
            ).also {
                mediaList.add(0, it)
            }
        }

        if (isAudioFileSupported(file)) {
            val newName = uniqueName(file.nameWithoutExtension, file.extension, DataFolder.audioFolder)
            val newFile = DataFolder.audioFolder.resolve("$newName.${file.extension}")
            file.copyTo(newFile)

            val cover = getAudioCover(newFile)

            return AudioInfo(
                id = mediaList.uniqueId(),
                path = newFile.path,
                name = newFile.name,
                duration = getAudioDuration(file) ?: return null,
                thumbnailID = null,
                thumbnailWidth = cover?.width,
                thumbnailHeight = cover?.height,
            ).also {
                mediaList.add(0, it)
            }
        }

        return null*/
    }

    fun addMedia(path: String, createCopy: Boolean = true): MediaInfo? = addMedia(File(path), createCopy)

    fun addImageMedia(image: ImageBitmap, name: String = "image_file"): MediaInfo {
        val newName = uniqueName(name.ifBlank { "image_file" }.toFileName(), "png", DataFolder.imageFolder)
        val newFile = DataFolder.imageFolder.resolve("$newName.png")
        image.savePngTo(newFile)

        return ImageInfo(
            id = mediaList.uniqueId(),
            path = newFile.path,
            name = newFile.name,
            width = image.width,
            height = image.height,
        ).also {
            mediaList.add(0, it)
        }
    }

    fun delete(mediaToDelete: List<MediaInfo>) {
        mediaList.removeAll(mediaToDelete)
        mediaGroups.forEach { ig -> ig.paths.removeAll(mediaToDelete.map { it.path }) }
        mediaToDelete.forEach { it.delete() }
        Properties.saveData()
    }

    fun deleteGroups(groupsToDelete: List<MediaGroup>) {
        mediaGroups.removeAll(groupsToDelete)
        Properties.saveData()
    }

    fun findMedia(path: String): MediaInfo? = mediaList.find { it.path == path }

    fun findMedia(id: Int): MediaInfo? = mediaList.find { it.id == id }

    @Deprecated("Returns incorrect result, don't use it")
    fun countSize(): Long {
        var sizeOfImageInfoInBytes = 0L

        mediaList.forEach { image ->
            sizeOfImageInfoInBytes += image.name.encodeToByteArray().size
            sizeOfImageInfoInBytes += image.path.encodeToByteArray().size
            sizeOfImageInfoInBytes += image.description.encodeToByteArray().size

            image.tags.forEach {
                sizeOfImageInfoInBytes += it.encodeToByteArray().size
            }

            sizeOfImageInfoInBytes += 1 // width
            sizeOfImageInfoInBytes += 1 // height
        }

        return sizeOfImageInfoInBytes
    }

    fun countThumbnailSize(): Long = mediaList
        .filter { it.isThumbnailLoaded }
        .sumOf { it.thumbnail!!.toPixelMap().buffer.size * 4L }

    fun copy(
        meta: String = this.dataVersion,
        mediaList: Collection<MediaInfo> = this.mediaList,
        mediaGroups: Collection<MediaGroup> = this.mediaGroups,
    ): MediaData {
        return MediaData(
            meta,
            mediaList.toMutableList(),
            mediaGroups.toMutableList(),
        )
    }
}