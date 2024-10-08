package composableFunctions.window

import info.ImageGroup
import info.ImageInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import colorText
import composableFunctions.views.ButtonText
import composableFunctions.TagGridWithCategories
import normalText
import padding
import properties.Properties
import settings
import toState

@Composable
fun EditImageWindow(
    imageInfo: ImageInfo,
    onDone: (ImageInfo) -> Unit = {},
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        var name by remember { mutableStateOf(TextFieldValue(imageInfo.name)) }
        var description by remember { mutableStateOf(TextFieldValue(imageInfo.description)) }
        val selectedTags = remember { imageInfo.tags.toState() }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(padding),
        ) {
            Text("Name:", color = colorText, fontSize = normalText)
            BasicTextField(
                name,
                onValueChange = { name = it },
                singleLine = true,
                maxLines = 1,
                textStyle = TextStyle(
                    color = colorText,
                    fontSize = normalText,
                ),
                cursorBrush = SolidColor(colorText),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding),
        ) {
            Text("Description:", color = colorText, fontSize = normalText)
            BasicTextField(
                description,
                onValueChange = { description = it },
                textStyle = TextStyle(
                    color = colorText,
                    fontSize = normalText,
                ),
                cursorBrush = SolidColor(colorText),
                modifier = Modifier.fillMaxWidth()
            )
        }

        TagGridWithCategories(
            tags = Properties.imagesData().tags,
            selectedTags = selectedTags,
            antiSelectedTags = emptyList(),
            expandable = false,
            onTagClick = {
                if (selectedTags.contains(it)) {
                    selectedTags.remove(it)
                } else {
                    selectedTags.add(it)
                    selectedTags.sort()
                }
            },
            onNew = {
                if (settings.autoSelectCreatedTags)
                    selectedTags.add(it)
            },
            modifier = Modifier.weight(1F),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            ButtonText(
                "Cancel",
                onClick = onCancel
            )

            ButtonText(
                "Save",
                onClick = {
                    onDone(
                        ImageInfo(
                            id = imageInfo.id,
                            path = imageInfo.path,
                            width = imageInfo.width,
                            height = imageInfo.height,
                            name = name.text,
                            description = description.text,
                            favorite = imageInfo.favorite,
                            tags = selectedTags,
                        )
                    )
                },
            )
        }
    }
}

@Composable
fun EditImageGroupWindow(
    imageGroup: ImageGroup,
    onDone: (ImageGroup) -> Unit = {},
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        var name by remember { mutableStateOf(TextFieldValue(imageGroup.name)) }
        var description by remember { mutableStateOf(TextFieldValue(imageGroup.description)) }
        val selectedTags = remember { imageGroup.tags.toState() }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(padding),
        ) {
            Text("Name:", color = colorText, fontSize = normalText)
            BasicTextField(
                name,
                onValueChange = { name = it },
                singleLine = true,
                maxLines = 1,
                textStyle = TextStyle(
                    color = colorText,
                    fontSize = normalText,
                ),
                cursorBrush = SolidColor(colorText),
                modifier = Modifier.weight(1F),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding),
        ) {
            Text("Description:", color = colorText, fontSize = normalText)
            BasicTextField(
                description,
                onValueChange = { description = it },
                textStyle = TextStyle(
                    color = colorText,
                    fontSize = normalText,
                ),
                cursorBrush = SolidColor(colorText),
                modifier = Modifier.weight(1F),
            )
        }

        TagGridWithCategories(
            tags = Properties.imagesData().tags,
            selectedTags = selectedTags,
            antiSelectedTags = emptyList(),
            expandable = false,
            onTagClick = {
                if (selectedTags.contains(it)) {
                    selectedTags.remove(it)
                } else {
                    selectedTags.add(it)
                    selectedTags.sort()
                }
            },
            onNew = {
                if (settings.autoSelectCreatedTags)
                    selectedTags.add(it)
            },
            modifier = Modifier.weight(1F),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            ButtonText(
                "Cancel",
                onClick = onCancel
            )

            ButtonText(
                "Save",
                onClick = {
                    onDone(
                        ImageGroup(
                            id = imageGroup.id,
                            imagePaths = imageGroup.imagePaths,
                            name = name.text,
                            description = description.text,
                            favorite = imageGroup.favorite,
                            tags = selectedTags,
                        )
                    )
                },
            )
        }
    }
}

@Composable
fun EditImageTagsWindow(
    newTags: List<String>,
    removeTags: List<String>,
    onTagClick: (String) -> Unit = {},
    onNew: (String) -> Unit = {},
    onDone: () -> Unit = {},
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        TagGridWithCategories(
            tags = Properties.imagesData().tags,
            selectedTags = newTags,
            antiSelectedTags = removeTags,
            expandable = false,
            onTagClick = onTagClick,
            onNew = onNew,
            modifier = Modifier.weight(1F),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(padding),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            ButtonText(
                "Cancel",
                onClick = onCancel
            )

            ButtonText(
                "Save",
                onClick = onDone,
            )
        }
    }
}