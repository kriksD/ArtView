package composableFunctions.screens

import ImageStorage
import TagStorage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import colorBackgroundLighter
import composableFunctions.ImageGrid
import composableFunctions.TagGridWithCategories
import loader.ImageLoader
import properties.Properties

@Composable
fun ImageGridScreen(
    imageLoader: ImageLoader,
    imageStorage: ImageStorage,
    tagStorage: TagStorage,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
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

        ImageGrid(
            images = imageStorage.filteredImages,
            imageLoader = imageLoader,
            checkedList = imageStorage.selectedImages,
            onCheckedClick = { imgInfo, isSelected ->
                if (isSelected) {
                    imageStorage.select(imgInfo)
                } else {
                    imageStorage.deselect(imgInfo)
                }
            },
            onOpen = {
                if (imageStorage.selectedImages.isEmpty()) {
                    imageStorage.open(it)
                } else {
                    if (!imageStorage.selectedImages.contains(it)) {
                        imageStorage.select(it)
                    } else {
                        imageStorage.deselect(it)
                    }
                }
            },
            modifier = Modifier.weight(1F),
        )
    }
}