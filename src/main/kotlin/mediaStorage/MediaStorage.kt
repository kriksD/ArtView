package mediaStorage

import androidx.compose.runtime.*
import formatFileNameDate
import getLastNPartsOfPath
import info.group.MediaGroup
import info.media.MediaInfo
import info.media.serializers.MediaInfoSerializer
import io.ktor.util.date.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import mediaData
import properties.DataFolder
import properties.Properties
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
            filteredMedia.find { it.path == opened?.path }
        }

        selectedGroups.clear()
        if (withGroups) {
            filterGroups(lastFilter)
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
            selectedMedia.forEach {
                val path = getLastNPartsOfPath(it.path, 2)
                val file = File(it.path)
                if (!file.exists()) return@forEach

                zip.putNextEntry(ZipEntry(path))
                zip.write(file.readBytes())
                zip.closeEntry()
            }

            zip.putNextEntry(ZipEntry("media_data.json"))
            zip.write(json.encodeToString(ListSerializer(MediaInfoSerializer), selectedMedia.toList()).toByteArray())
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
        mediaData.mediaGroups.add(newGroup)
        Properties.saveData()
        update()
    }
}