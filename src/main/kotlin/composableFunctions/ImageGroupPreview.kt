package composableFunctions

import ImageGroup
import ImageInfo
import ImageLoader
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import bigIconSize
import bigText
import biggerPadding
import colorBackgroundLighter
import colorText
import iconSize
import normalText
import padding
import properties.Properties
import toState

@Composable
fun ImageGroupPreview(
    imageGroup: ImageGroup,
    imageLoader: ImageLoader,
    onClose: () -> Unit = {},
    onEdit: () -> Unit = {},
    onImageSelected: (ImageInfo, List<ImageInfo>) -> Unit = { _, _ -> },
    onSelectedUpdated: (List<ImageInfo>) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val images = remember { Properties.imagesData().images.filter { imageGroup.imagePaths.contains(it.path) }.toState() }
    var filteredImages by remember { mutableStateOf(images.toList()) }
    val selectedImages = remember { mutableStateListOf<ImageInfo>() }

    val selectedTags = remember { mutableStateListOf<String>() }
    val antiSelectedTags = remember { mutableStateListOf<String>() }

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = colorText,
                modifier = Modifier
                    .size(bigIconSize)
                    .clickable(onClick = onClose)
                    .padding(horizontal = biggerPadding, vertical = padding)
            )
            Text(imageGroup.name, color = colorText, fontSize = bigText, modifier = Modifier.weight(1F))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = colorText,
                modifier = Modifier
                    .size(iconSize)
                    .clickable(onClick = onEdit)
            )
        }
        Text(imageGroup.description, color = colorText, fontSize = normalText)

        var expanded by remember { mutableStateOf(false) }
        Column {
            TagTableWithCategories(
                tags = Properties.imagesData().tags,
                selectedTags = selectedTags,
                antiSelectedTags = antiSelectedTags,
                onTagClick = {
                    if (selectedTags.contains(it)) {
                        selectedTags.remove(it)
                        antiSelectedTags.add(it)

                    } else if (antiSelectedTags.contains(it)) {
                        selectedTags.remove(it)
                        antiSelectedTags.remove(it)

                    } else {
                        selectedTags.add(it)
                        antiSelectedTags.remove(it)
                    }
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorBackgroundLighter),
            )

            ImageGridWithLoading(
                images = images.filter { image ->
                    val hasSelectedTags = selectedTags.all { tag -> image.tags.contains(tag) }
                    val hasAntiSelectedTags = antiSelectedTags.none { tag -> image.tags.contains(tag) }
                    hasSelectedTags && hasAntiSelectedTags

                }.also { filteredImages = it },
                imageLoader = imageLoader,
                checkedList = selectedImages,
                onCheckedClick = { imgInfo, isSelected ->
                    if (isSelected) {
                        selectedImages.add(imgInfo)
                    } else {
                        selectedImages.remove(imgInfo)
                    }

                    onSelectedUpdated(selectedImages)
                },
                onOpen = {
                    if (selectedImages.isEmpty()) {
                        onImageSelected(it, filteredImages)
                    } else {
                        if (selectedImages.contains(it)) {
                            selectedImages.remove(it)
                        } else {
                            selectedImages.add(it)
                        }
                    }
                },
                modifier = Modifier.weight(1F),
            )
        }
    }
}