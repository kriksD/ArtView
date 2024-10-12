package composableFunctions.window

import info.MediaGroup
import info.MediaInfo
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
    mediaInfo: MediaInfo,
    onDone: (MediaInfo) -> Unit = {},
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        var name by remember { mutableStateOf(TextFieldValue(mediaInfo.name)) }
        var description by remember { mutableStateOf(TextFieldValue(mediaInfo.description)) }
        val selectedTags = remember { mediaInfo.tags.toState() }

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
            tags = Properties.mediaData().tags,
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
                        MediaInfo(
                            id = mediaInfo.id,
                            path = mediaInfo.path,
                            width = mediaInfo.width,
                            height = mediaInfo.height,
                            name = name.text,
                            description = description.text,
                            favorite = mediaInfo.favorite,
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
    mediaGroup: MediaGroup,
    onDone: (MediaGroup) -> Unit = {},
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        var name by remember { mutableStateOf(TextFieldValue(mediaGroup.name)) }
        var description by remember { mutableStateOf(TextFieldValue(mediaGroup.description)) }
        val selectedTags = remember { mediaGroup.tags.toState() }

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
            tags = Properties.mediaData().tags,
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
                        MediaGroup(
                            id = mediaGroup.id,
                            paths = mediaGroup.paths,
                            name = name.text,
                            description = description.text,
                            favorite = mediaGroup.favorite,
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
            tags = Properties.mediaData().tags,
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