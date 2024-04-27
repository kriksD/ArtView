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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import bigText
import biggerPadding
import colorBackground
import colorBackgroundLighter
import colorText
import colorTextSecond
import corners
import iconSize
import normalText
import padding
import properties.Properties
import properties.settings.TagControlsPosition
import settings
import smallCorners
import tinyIconSize

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TagSelectionByDefault(modifier = Modifier.fillMaxWidth())
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            TagControlsPositionOptions(modifier = Modifier.fillMaxWidth())
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
                        //reload = true
                    },
                    onUp = {
                        Properties.imagesData().moveCategoryUp(category.name)
                        Properties.saveData()
                        //reload = true
                    },
                    onDown = {
                        Properties.imagesData().moveCategoryDown(category.name)
                        Properties.saveData()
                        //reload = true
                    },
                    upAvailable = tags.indexOf(category) > 0,
                    downAvailable = tags.indexOf(category) < tags.lastIndex,
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
    onRemove: () -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    upAvailable: Boolean,
    downAvailable: Boolean,
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                if (upAvailable) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Up",
                        tint = colorText,
                        modifier = Modifier
                            .size(tinyIconSize)
                            .clickable { onUp() },
                    )
                }

                if (downAvailable) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Down",
                        tint = colorText,
                        modifier = Modifier
                            .size(tinyIconSize)
                            .clickable { onDown() },
                    )
                }
            }

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
            } else {
                Spacer(
                    modifier = Modifier
                        .size(iconSize)
                        .padding(horizontal = biggerPadding, vertical = padding),
                )
            }
        }
    }
}

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

@Composable
private fun TagControlsPositionOptions(
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Text("Tag controls position:", color = colorText, fontSize = bigText)

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Add button:",
                color = colorText,
                fontSize = normalText,
                modifier = Modifier
            )

            TagControlsPositionMenu(
                value = settings.addTagButtonPosition,
                onSelect = {
                    settings.addTagButtonPosition = it
                    Properties.saveSettings()
                },
                modifier = Modifier
                    .padding(padding),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Filter button:",
                color = colorText,
                fontSize = normalText,
                modifier = Modifier
            )

            TagControlsPositionMenu(
                value = settings.filterTagButtonPosition,
                onSelect = {
                    settings.filterTagButtonPosition = it
                    Properties.saveSettings()
                },
                modifier = Modifier
                    .padding(padding),
            )
        }
    }
}

@Composable
private fun TagControlsPositionMenu(
    value: TagControlsPosition,
    onSelect: (TagControlsPosition) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Menu(
        value,
        TagControlsPosition.entries,
        onSelect = { onSelect(it as TagControlsPosition) },
        itemContent = { item, type ->
            Text(
                (item as TagControlsPosition).toString(),
                color = if (type == MenuItemType.ListSelected) colorTextSecond else colorText,
                fontSize = normalText,
                modifier = Modifier
                    .background(colorBackground, RoundedCornerShape(corners))
                    .padding(padding),
            )
        },
        modifier = modifier,
    )
}