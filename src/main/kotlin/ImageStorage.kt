import androidx.compose.runtime.*
import properties.Properties
import java.io.File

class ImageStorage {
    val filteredImages = mutableStateListOf<ImageInfo>()
    val selectedImages = mutableStateListOf<ImageInfo>()
    private var opened by mutableStateOf<ImageInfo?>(null)

    var withGroups by mutableStateOf(false)
    val filteredGroups = mutableStateListOf<ImageGroup>()
    val selectedGroups = mutableStateListOf<ImageGroup>()
    private var openedGroup by mutableStateOf<ImageGroup?>(null)

    private var lastFilter: FilterBuilder = FilterBuilder()

    fun setFilter(filter: FilterBuilder) {
        lastFilter = filter
    }

    fun updateFilterTags(tags: List<String>, antiTags: List<String>) {
        filter(lastFilter.antiTags(antiTags).tags(tags))
    }

    fun filter(filter: FilterBuilder) {
        filteredImages.clear()
        selectedImages.clear()
        filteredImages.addAll(filter.filter(Properties.imagesData().images))
        lastFilter = filter
    }

    fun filterGroups(filter: FilterBuilder) {
        filteredGroups.clear()
        selectedGroups.clear()
        filteredGroups.addAll(filter.filterGroups(Properties.imagesData().imageGroups))
        lastFilter = filter
    }

    fun update() {
        selectedImages.clear()
        opened = null
        filter(lastFilter)

        if (withGroups) {
            filterGroups(lastFilter)
        } else {
            filteredGroups.clear()
            selectedGroups.clear()
            openedGroup = null
        }
    }

    fun select(image: ImageInfo) = selectedImages.add(image)
    fun deselect(image: ImageInfo) = selectedImages.remove(image)

    fun select(group: ImageGroup) = selectedGroups.add(group)
    fun deselect(group: ImageGroup) = selectedGroups.remove(group)

    fun selectAll() = selectedImages.addAll(filteredImages)
    fun deselectAll() = selectedImages.clear()

    fun selectAllGroups() = selectedGroups.addAll(filteredGroups)
    fun deselectAllGroups() = selectedGroups.clear()

    fun open(image: ImageInfo) { opened = image }
    fun close() { opened = null }
    fun next() { opened = filteredImages.getOrNull(filteredImages.indexOf(opened) + 1) }
    fun previous() { opened = filteredImages.getOrNull(filteredImages.indexOf(opened) - 1) }
    val openedImage: ImageInfo? get() = opened

    fun openGroup(group: ImageGroup) { openedGroup = group }
    fun closeGroup() { openedGroup = null }
    val openedImageGroup: ImageGroup? get() = openedGroup

    fun reset() {
        withGroups = false
        update()
        deselectAll()
        deselectAllGroups()
        close()
        closeGroup()
    }

    fun delete(images: List<ImageInfo>) {
        Properties.imagesData().images.removeAll(images)
        Properties.imagesData().imageGroups.forEach { ig -> ig.imagePaths.removeAll(images.map { it.path }) }
        images.forEach { it.delete() }
        Properties.saveData()
        update()
    }

    fun saveImageFilesTo(folder: File = File("images_filtered")) {
        folder.mkdir()
        folder.listFiles()?.forEach { it.delete() }

        if (selectedImages.isNotEmpty()) {
            selectedImages.forEach { it.saveFileTo(folder) }

        } else if (selectedGroups.isNotEmpty()) {
            selectedGroups.forEach { it.saveImageFilesTo(folder) }
        }
    }

    fun createNewGroup() {
        val newGroup = ImageGroup(selectedImages.map { it.path }.toMutableList())
        Properties.imagesData().imageGroups.add(newGroup)
        Properties.saveData()
        update()
    }
}