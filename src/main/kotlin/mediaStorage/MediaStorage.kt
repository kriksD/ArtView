package mediaStorage

import androidx.compose.runtime.*
import formatFileNameDate
import info.group.MediaGroup
import info.media.MediaInfo
import io.ktor.util.date.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mediaData
import properties.DataFolder
import properties.Properties
import properties.data.MediaData
import settings
import tag.TagStorage
import uniqueName
import utilities.uniqueId
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class MediaStorage {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    val filteredMedia = mutableStateListOf<MediaInfo>()
    val selectedMedia = mutableStateListOf<MediaInfo>()
    private var opened by mutableStateOf<MediaInfo?>(null)

    var withGroups by mutableStateOf(false)
    val filteredGroups = mutableStateListOf<MediaGroup>()
    val selectedGroups = mutableStateListOf<MediaGroup>()
    private var openedGroup by mutableStateOf<MediaGroup?>(null)

    private var lastFilter: Filter = Filter()
    val currentFilter get() = lastFilter

    fun setFilter(filter: Filter) {
        lastFilter = filter
    }

    fun updateFilterTags(tagStorage: TagStorage) {
        filter(lastFilter.tags(tagStorage))
    }

    fun filter(filter: Filter) {
        filteredMedia.clear()
        selectedMedia.clear()
        filteredMedia.addAll(filter.filter(mediaData.mediaList))
        lastFilter = filter
    }

    fun filterGroups(filter: Filter) {
        filteredGroups.clear()
        selectedGroups.clear()
        filteredGroups.addAll(filter.filterGroups(mediaData.mediaGroups))
        lastFilter = filter
    }

    fun update(withoutClosing: Boolean = false) {
        selectedMedia.clear()
        filter(lastFilter)
        opened = if (!withoutClosing) {
            null
        } else {
            filteredMedia.find { it.id == opened?.id }
        }

        selectedGroups.clear()
        if (withGroups) {
            filterGroups(lastFilter)
            openedGroup = if (!withoutClosing) {
                null
            } else {
                filteredGroups.find { it.id == openedGroup?.id }
            }
        } else {
            filteredGroups.clear()
            openedGroup = null
        }
    }

    fun select(media: MediaInfo) = selectedMedia.add(media)
    fun deselect(media: MediaInfo) = selectedMedia.remove(media)

    fun select(group: MediaGroup) = selectedGroups.add(group)
    fun deselect(group: MediaGroup) = selectedGroups.remove(group)

    fun selectAll() = selectedMedia.also { it.clear() }.addAll(filteredMedia)
    fun deselectAll() = selectedMedia.clear()

    fun selectAllGroups() = selectedGroups.also { it.clear() }.addAll(filteredGroups)
    fun deselectAllGroups() = selectedGroups.clear()

    fun open(media: MediaInfo) { opened = media }
    fun close() { opened = null }
    fun next() { opened = filteredMedia.getOrNull(filteredMedia.indexOf(opened) + 1) }
    fun previous() { opened = filteredMedia.getOrNull(filteredMedia.indexOf(opened) - 1) }
    val openedMedia: MediaInfo? get() = opened

    fun openGroup(group: MediaGroup) { openedGroup = group }
    fun closeGroup() { openedGroup = null }
    val openedMediaGroup: MediaGroup? get() = openedGroup

    fun reset() {
        withGroups = false
        update()
        deselectAll()
        deselectAllGroups()
        close()
        closeGroup()
    }

    fun delete(mediaList: List<MediaInfo>) {
        mediaData.delete(mediaList)
        update()
    }

    fun deleteGroups(groups: List<MediaGroup>) {
        mediaData.deleteGroups(groups)
        update()
    }

    fun saveSelectedMediaTo(folder: File = DataFolder.filteredFolder) {
        folder.mkdirs()

        val fileName = uniqueName("filtered_${getTimeMillis().formatFileNameDate()}", "zip", folder)
        val zipFile = folder.resolve("$fileName.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
            val mediaToSave = (selectedMedia.toList() +
                        selectedGroups.flatMap { it.mediaIDs }.mapNotNull { mediaData.findMedia(it) })
                .distinctBy { it.id }

            mediaToSave.forEach {
                val file = File(it.path)
                if (!file.exists()) return@forEach

                zip.putNextEntry(ZipEntry(file.relativeTo(DataFolder.folder).path))
                file.inputStream().use { input ->
                    input.copyTo(zip)
                }
                zip.closeEntry()
            }

            val selectedData = MediaData(
                dataVersion = mediaData.dataVersion,
                mediaList = mediaToSave,
                mediaGroups = selectedGroups.toList(),
            )

            zip.putNextEntry(ZipEntry("media_data.json"))
            zip.write(json.encodeToString(selectedData).toByteArray())
            zip.closeEntry()
        }
    }

    fun createNewGroup() {
        val tags = if (settings.addTagsToCreatedGroups) {
            selectedMedia.map { it.tags }.flatten().distinct().toMutableList()
        } else {
            mutableListOf()
        }

        val newGroup = MediaGroup(
            id = mediaData.mediaGroups.uniqueId(),
            mediaIDs = selectedMedia.map { it.id }.toMutableList(),
            tags = tags,
        )
        mediaData.mediaGroups.add(0, newGroup)
        Properties.saveData()
        update()
    }
}