package mediaStorage

import androidx.compose.runtime.*
import info.group.MediaGroup
import info.media.MediaInfo
import mediaData
import properties.DataFolder
import properties.Properties
import settings
import tag.TagStorage
import utilities.uniqueId
import java.io.File

class MediaStorage {
    val filteredMedia = mutableStateListOf<MediaInfo>()
    val selectedMedia = mutableStateListOf<MediaInfo>()
    private var opened by mutableStateOf<MediaInfo?>(null)

    var withGroups by mutableStateOf(false)
    val filteredGroups = mutableStateListOf<MediaGroup>()
    val selectedGroups = mutableStateListOf<MediaGroup>()
    private var openedGroup by mutableStateOf<MediaGroup?>(null)

    private var lastFilter: Filter = Filter()

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

    fun saveMediaFilesTo(folder: File = DataFolder.filteredFolder) {
        folder.mkdirs()
        folder.listFiles()?.forEach { it.delete() }

        if (selectedMedia.isNotEmpty()) {
            selectedMedia.forEach { it.saveFileTo(folder) }

        } else if (selectedGroups.isNotEmpty()) {
            selectedGroups.forEach { it.saveImageFilesTo(folder) }
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