package composableFunctions.screens

import Filter
import ImageStorage
import TagStorage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import colorBackgroundLighter
import composableFunctions.ImageGroupGrid
import composableFunctions.ImageGroupPreview
import composableFunctions.TagGridWithCategories
import loader.ImageLoader
import properties.Properties

@Composable
fun GroupGridScreen(
    imageLoader: ImageLoader,
    imageStorage: ImageStorage,
    tagStorage: TagStorage,
    onGroupEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (imageStorage.openedImageGroup == null) {
        var expanded by remember { mutableStateOf(false) }
        Column(
            modifier = modifier,
        ) {
            TagGridWithCategories(
                tags = Properties.imagesData().tags,
                selectedTags = tagStorage.selectedTags,
                antiSelectedTags = tagStorage.selectedAntiTags,
                onTagClick = {
                    tagStorage.changeSelectStatus(it)
                    imageStorage.updateFilterTags(tagStorage)
                    imageStorage.update()
                    imageLoader.reset()
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorBackgroundLighter),
            )
            ImageGroupGrid(
                imageGroups = imageStorage.filteredGroups,
                imageLoader = imageLoader,
                checkedList = imageStorage.selectedGroups,
                onCheckedClick = { imgGroup, isSelected ->
                    if (isSelected) {
                        imageStorage.select(imgGroup)
                    } else {
                        imageStorage.deselect(imgGroup)
                    }
                },
                onOpen = {
                    if (imageStorage.selectedGroups.isEmpty()) {
                        imageStorage.openGroup(it)
                        imageStorage.filter(
                            Filter().tags(tagStorage).group(it)
                        )
                    } else {
                        if (!imageStorage.selectedGroups.contains(it)) {
                            imageStorage.select(it)
                        } else {
                            imageStorage.deselect(it)
                        }
                    }
                },
                modifier = Modifier.weight(1F),
            )
        }
    } else {
        ImageGroupPreview(
            tagStorage = tagStorage,
            imageStorage = imageStorage,
            imageLoader = imageLoader,
            onClose = {
                imageStorage.openedImageGroup?.getImageInfoList()?.forEach { imageLoader.unloadNext(it) }
                imageStorage.closeGroup()
            },
            onEdit = { onGroupEdit() },
            onDelete = {
                imageStorage.deleteGroups(listOfNotNull(imageStorage.openedImageGroup))
                imageStorage.closeGroup()
            },
            modifier = modifier,
        )
    }
}