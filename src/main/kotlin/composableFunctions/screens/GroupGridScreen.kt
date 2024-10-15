package composableFunctions.screens

import mediaStorage.Filter
import mediaStorage.MediaStorage
import tag.TagStorage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import colorBackgroundLighter
import composableFunctions.ImageGroupGrid
import composableFunctions.ImageGroupPreview
import composableFunctions.TagGridWithCategories
import loader.ThumbnailLoader
import tagData

@Composable
fun GroupGridScreen(
    thumbnailLoader: ThumbnailLoader,
    mediaStorage: MediaStorage,
    tagStorage: TagStorage,
    onGroupEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (mediaStorage.openedMediaGroup == null) {
        var expanded by remember { mutableStateOf(false) }
        Column(
            modifier = modifier,
        ) {
            TagGridWithCategories(
                tags = tagData.tags,
                selectedTags = tagStorage.selectedTags,
                antiSelectedTags = tagStorage.selectedAntiTags,
                onTagClick = {
                    tagStorage.changeSelectStatus(it)
                    mediaStorage.updateFilterTags(tagStorage)
                    mediaStorage.update()
                    thumbnailLoader.reset()
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorBackgroundLighter),
            )
            ImageGroupGrid(
                mediaGroups = mediaStorage.filteredGroups,
                thumbnailLoader = thumbnailLoader,
                checkedList = mediaStorage.selectedGroups,
                onCheckedClick = { imgGroup, isSelected ->
                    if (isSelected) {
                        mediaStorage.select(imgGroup)
                    } else {
                        mediaStorage.deselect(imgGroup)
                    }
                },
                onOpen = {
                    if (mediaStorage.selectedGroups.isEmpty()) {
                        mediaStorage.openGroup(it)
                        mediaStorage.filter(
                            Filter().tags(tagStorage).group(it)
                        )
                    } else {
                        if (!mediaStorage.selectedGroups.contains(it)) {
                            mediaStorage.select(it)
                        } else {
                            mediaStorage.deselect(it)
                        }
                    }
                },
                modifier = Modifier.weight(1F),
            )
        }
    } else {
        ImageGroupPreview(
            tagStorage = tagStorage,
            imageStorage = mediaStorage,
            thumbnailLoader = thumbnailLoader,
            onClose = {
                mediaStorage.openedMediaGroup?.getImageInfoList()?.forEach { thumbnailLoader.unloadNext(it) }
                mediaStorage.closeGroup()
            },
            onEdit = { onGroupEdit() },
            onDelete = {
                mediaStorage.deleteGroups(listOfNotNull(mediaStorage.openedMediaGroup))
                mediaStorage.closeGroup()
            },
            modifier = modifier,
        )
    }
}