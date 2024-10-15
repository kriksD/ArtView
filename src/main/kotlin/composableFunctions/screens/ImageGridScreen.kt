package composableFunctions.screens

import mediaStorage.MediaStorage
import tag.TagStorage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import colorBackgroundLighter
import composableFunctions.ImageGrid
import composableFunctions.TagGridWithCategories
import loader.ThumbnailLoader
import tagData

@Composable
fun ImageGridScreen(
    thumbnailLoader: ThumbnailLoader,
    mediaStorage: MediaStorage,
    tagStorage: TagStorage,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
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

        ImageGrid(
            mediaList = mediaStorage.filteredMedia,
            thumbnailLoader = thumbnailLoader,
            checkedList = mediaStorage.selectedMedia,
            onCheckedClick = { imgInfo, isSelected ->
                if (isSelected) {
                    mediaStorage.select(imgInfo)
                } else {
                    mediaStorage.deselect(imgInfo)
                }
            },
            onOpen = {
                if (mediaStorage.selectedMedia.isEmpty()) {
                    mediaStorage.open(it)
                } else {
                    if (!mediaStorage.selectedMedia.contains(it)) {
                        mediaStorage.select(it)
                    } else {
                        mediaStorage.deselect(it)
                    }
                }
            },
            modifier = Modifier.weight(1F),
        )
    }
}