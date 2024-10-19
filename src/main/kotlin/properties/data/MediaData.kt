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
import tagData
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

    fun addMedia(file: File, createCopy: Boolean = true) {
        if (file.extension == "zip") {
            val newData = MediaInfoFactory.makeFromZip(file, mediaList, mediaGroups) ?: return
            mediaList.addAll(0, newData.mediaList)
            mediaGroups.addAll(0, newData.mediaGroups)

            val tags = (newData.mediaList.flatMap { it.tags } + newData.mediaGroups.flatMap { it.tags }).distinct()
            tagData.addAllTags(tags, "New Unknown Tags")

        } else {
            val mediaInfo = MediaInfoFactory.makeFromFile(file, mediaList, createCopy) ?: return
            mediaList.add(0, mediaInfo)
        }
    }

    fun addMedia(path: String, createCopy: Boolean = true) = addMedia(File(path), createCopy)

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
        mediaGroups.forEach { ig -> ig.mediaIDs.removeAll(mediaToDelete.map { it.id }) }
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