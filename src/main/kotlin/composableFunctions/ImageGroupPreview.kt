package composableFunctions

import loader.ImageLoader
import ImageStorage
import TagStorage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import transparencySecond

@Composable
fun ImageGroupPreview(
    tagStorage: TagStorage,
    imageStorage: ImageStorage,
    imageLoader: ImageLoader,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = colorText,
                modifier = Modifier
                    .size(bigIconSize)
                    .clickable(onClick = onClose)
                    .padding(horizontal = biggerPadding, vertical = padding)
            )

            Text(imageStorage.openedImageGroup?.name ?: "<ERROR>", color = colorText, fontSize = bigText, modifier = Modifier.weight(1F))

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
        Text(imageStorage.openedImageGroup?.description ?: "<ERROR>", color = colorText, fontSize = normalText)

        var expanded by remember { mutableStateOf(false) }
        Column {
            TagGridWithCategories(
                tags = Properties.imagesData().tags,
                selectedTags = tagStorage.selectedTags,
                antiSelectedTags = tagStorage.selectedAntiTags,
                onTagClick = {
                    tagStorage.changeSelectStatus(it)
                    imageStorage.updateFilterTags(tagStorage)
                    imageStorage.update()
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
}