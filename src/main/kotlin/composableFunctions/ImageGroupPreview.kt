package composableFunctions

import ImageGroup
import ImageInfo
import ImageLoader
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import bigIconSize
import bigText
import biggerPadding
import colorBackground
import colorBackgroundLighter
import colorBackgroundSecondLighter
import colorText
import corners
import iconSize
import normalText
import padding
import properties.Properties
import toState
import transparencySecond

@Composable
fun ImageGroupPreview(
    imageGroup: ImageGroup,
    imageLoader: ImageLoader,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onImageSelected: (ImageInfo, List<ImageInfo>) -> Unit,
    onSelectedUpdated: (List<ImageInfo>) -> Unit,
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

            var toDelete by remember { mutableStateOf(false) }
            Icon(
                Icons.Default.Delete,
                contentDescription = "delete image",
                tint = colorText,
                modifier = Modifier
                    .size(iconSize)
                    .clickable { toDelete = true }
            )

            Dialog(
                visible = toDelete,
                onCloseRequest = { toDelete = false },
                resizable = false,
                undecorated = true,
                transparent = true,
            ) {
                Column(
                    modifier = Modifier
                        .background(colorBackground.copy(transparencySecond), RoundedCornerShape(corners))
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "You are going to delete this group forever. Are you sure?",
                        color = colorText,
                        fontSize = normalText,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(padding)
                    ) {
                        Button(
                            onClick = { toDelete = false },
                            colors = ButtonDefaults.buttonColors(backgroundColor = colorBackgroundSecondLighter),
                        ) {
                            Text("No", color = colorText, fontSize = normalText)
                        }

                        Button(
                            onClick = {
                                onDelete()
                                toDelete = false
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = colorBackgroundSecondLighter),
                        ) {
                            Text("Yes", color = colorText, fontSize = normalText)
                        }
                    }
                }
            }

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

            ImageGrid(
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