import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import properties.Properties

@Composable
fun EditImageWindow(
    tags: List<String>,
    imageInfo: ImageInfo,
    onDone: (ImageInfo) -> Unit = {},
    onCancel: () -> Unit = {},
    onNewTag: (String) -> Unit = {},
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

        TagTable(
            tags = tags,
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
            onNew = onNewTag,
            modifier = Modifier
                .weight(1F)
                .verticalScroll(rememberScrollState()),
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
                            path = imageInfo.path,
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
fun EditImageTagsWindow(
    tags: List<String>,
    newTags: List<String>,
    removeTags: List<String>,
    onTagClick: (String) -> Unit = {},
    onNewTag: (String) -> Unit = {},
    onDone: () -> Unit = {},
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        TagTable(
            tags = tags,
            selectedTags = newTags,
            antiSelectedTags = removeTags,
            expandable = false,
            onTagClick = onTagClick,
            onNew = onNewTag,
            modifier = Modifier
                .weight(1F)
                .verticalScroll(rememberScrollState()),
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