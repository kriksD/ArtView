package composableFunctions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import bigText
import biggerPadding
import colorBackground
import colorBackgroundLighter
import colorText
import iconSize
import normalText
import padding
import properties.Properties
import settings
import smallCorners

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TagSelectionByDefault(modifier = Modifier.fillMaxWidth())
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Options(modifier = Modifier.fillMaxWidth())
            TagsCategories(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Options(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Text("Options:", color = colorText, fontSize = bigText)

        CheckboxText(
            text = "Auto select created tags",
            value = settings.autoSelectCreatedTags,
            onValueChange = {
                settings.autoSelectCreatedTags = it
                Properties.saveSettings()
            },
        )

        CheckboxText(
            text = "Add tags to created groups",
            value = settings.addTagsToCreatedGroups,
            onValueChange = {
                settings.addTagsToCreatedGroups = it
                Properties.saveSettings()
            },
        )

        CheckboxText(
            text = "Show debug",
            value = settings.showDebug,
            onValueChange = {
                settings.showDebug = it
                Properties.saveSettings()
            },
        )
    }
}

@Composable
private fun TagsCategories(
    modifier: Modifier = Modifier,
) {
    var reload by remember { mutableStateOf(false) }

    if (!reload) {
        val tags = Properties.imagesData().tags

        Column(modifier = modifier) {
            Text("Tag Categories:", color = colorText, fontSize = bigText)

            tags.forEach { category ->
                TagCategoryCard(
                    category.name,
                    category.tags.size,
                    removable = category.name != "Other",
                    onRemove = {
                        Properties.imagesData().removeCategory(category.name)
                        Properties.saveData()
                        reload = true
                    },
                    modifier = Modifier.fillMaxWidth(0.5F),
                )
            }

            var newCategoryName by remember { mutableStateOf("") }
            NewTagCategoryCard(
                newCategoryName,
                onNameValueChange = {
                    newCategoryName = it
                },
                onCreate = {
                    Properties.imagesData().createCategory(newCategoryName)
                    Properties.saveData()

                    reload = true
                },
                modifier = Modifier.fillMaxWidth(0.5F),
            )
        }
    } else {
        reload = false
    }
}

@Composable
private fun TagCategoryCard(
    name: String,
    tagsCount: Int,
    removable: Boolean = true,
    onRemove: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(padding)
            .background(
                color = colorBackground,
                shape = RoundedCornerShape(smallCorners)
            )
            .padding(padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("$name - $tagsCount tags", color = colorText, fontSize = normalText)

        if (removable) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = colorText,
                modifier = Modifier
                    .size(iconSize)
                    .clickable(onClick = onRemove)
                    .padding(horizontal = biggerPadding, vertical = padding),
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun NewTagCategoryCard(
    nameValue: String,
    onNameValueChange: (String) -> Unit = {},
    onCreate: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(padding)
            .background(
                color = colorBackground,
                shape = RoundedCornerShape(smallCorners)
            )
            .padding(padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Create new category:", color = colorText, fontSize = normalText)
            BasicTextField(
                value = nameValue,
                onValueChange = onNameValueChange,
                textStyle = TextStyle(
                    color = colorText,
                    fontSize = normalText,
                ),
                cursorBrush = SolidColor(colorText),
                singleLine = true,
                maxLines = 1,
                modifier = Modifier
                    .padding(padding)
                    .background(
                        color = colorBackgroundLighter,
                        shape = RoundedCornerShape(smallCorners)
                    )
                    .padding(padding)
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                            onCreate()
                            return@onKeyEvent true
                        }

                        false
                    },
            )
        }

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = colorText,
            modifier = Modifier
                .size(iconSize)
                .clickable(onClick = onCreate)
                .padding(horizontal = biggerPadding, vertical = padding),
        )
    }
}

@Composable
private fun TagSelectionByDefault(
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text("Tag selection by default:", color = colorText, fontSize = bigText)

        TagTableWithCategories(
            Properties.imagesData().tags,
            settings.selectedTagsByDefault,
            settings.antiSelectedTagsByDefault,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onTagClick = {
                if (settings.selectedTagsByDefault.contains(it)) {
                    settings.selectedTagsByDefault.remove(it)
                    settings.antiSelectedTagsByDefault.add(it)

                } else if (settings.antiSelectedTagsByDefault.contains(it)) {
                    settings.selectedTagsByDefault.remove(it)
                    settings.antiSelectedTagsByDefault.remove(it)

                } else {
                    settings.selectedTagsByDefault.add(it)
                    settings.antiSelectedTagsByDefault.remove(it)
                }

                Properties.saveSettings()
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}