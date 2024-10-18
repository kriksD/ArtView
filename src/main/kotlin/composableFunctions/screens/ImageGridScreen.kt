package composableFunctions.screens

import mediaStorage.MediaStorage
import tag.TagStorage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import colorBackgroundLighter
import composableFunctions.ImageGrid
import composableFunctions.TagGridWithCategories
import composableFunctions.TypeSelector
import composableFunctions.views.CheckboxText
import info.media.MediaType
import loader.ThumbnailLoader
import padding
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

        Row {
            var selectedType by remember { mutableStateOf<MediaType?>(null) }
            TypeSelector(
                selected = selectedType,
                onSelected = {
                    selectedType = it
                    mediaStorage.filter(mediaStorage.currentFilter.type(selectedType))
                    thumbnailLoader.reset()
                },
                modifier = Modifier
                    .weight(1F),
            )

            var showHidden by remember { mutableStateOf(false) }
            CheckboxText(
                text = "Show hidden",
                value = showHidden,
                onValueChange = {
                    showHidden = it
                    mediaStorage.filter(mediaStorage.currentFilter.nonHidden(!showHidden))
                    thumbnailLoader.reset()
                },
                modifier = Modifier
                    .padding(end = padding),
            )
        }

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