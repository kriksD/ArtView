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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import bigText
import biggerPadding
import colorBackground
import colorBackgroundLighter
import colorText
import colorTextError
import colorTextSecond
import corners
import iconSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import normalAnimationDuration
import normalText
import padding
import properties.Properties
import properties.data.backup.BackupInfo
import properties.settings.TagControlsPosition
import roundPlaces
import runCommand
import settings
import smallCorners
import tinyIconSize
import toTimeString
import java.io.File

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(padding),
    ) {
        TagSelectionByDefault(modifier = Modifier.fillMaxWidth())
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            UsualDivider(modifier = Modifier.fillMaxWidth())
            TagControlsPositionOptions(modifier = Modifier.fillMaxWidth())
            Options(modifier = Modifier.fillMaxWidth())
            UsualDivider(modifier = Modifier.fillMaxWidth())
            TagsCategories(modifier = Modifier.fillMaxWidth())
            UsualDivider(modifier = Modifier.fillMaxWidth())
            BooruTagsCategories(modifier = Modifier.fillMaxWidth())
            UsualDivider(modifier = Modifier.fillMaxWidth())
            Backup(modifier = Modifier.fillMaxWidth())
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier,
        ) {
            val previousValue by remember { mutableStateOf(settings.imageLoadingThreads) }
            Menu(
                selectedItem = settings.imageLoadingThreads,
                items = listOf(1, 2, 3, 4, 5, 6),
                onSelect = {
                    settings.imageLoadingThreads = it as Int
                    Properties.saveSettings()
                },
                itemContent = { item, type ->
                    Text(
                        (item as Int).toString(),
                        color = if (type == MenuItemType.ListSelected) colorTextSecond else colorText,
                        fontSize = normalText,
                        modifier = Modifier
                            .background(colorBackground, RoundedCornerShape(corners))
                            .padding(top = padding, bottom = padding, start = biggerPadding, end = biggerPadding),
                    )
                },
                modifier = Modifier.padding(padding),
            )

            AppearDisappearAnimation(
                visible = previousValue != settings.imageLoadingThreads,
                duration = normalAnimationDuration,
            ) {
                Text(
                    text = "Relaunch the app to apply this change!",
                    color = colorTextError,
                    fontSize = normalText,
                )
            }
        }
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
                    .padding(top = padding, bottom = padding, start = biggerPadding, end = biggerPadding),
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun BooruTagsCategories(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Text("Booru tags categories:", color = colorText, fontSize = bigText)

        TextAndTextField(
            name = "Booru tags category:",
            value = settings.booruTagsCategoryName,
            onValueChange = {
                settings.booruTagsCategoryName = it
                Properties.saveSettings()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
        )

        TextAndTextField(
            name = "Characters category:",
            value = settings.characterTagsCategoryName,
            onValueChange = {
                settings.characterTagsCategoryName = it
                Properties.saveSettings()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
        )

        TextAndTextField(
            name = "Copyrights category:",
            value = settings.copyrightTagsCategoryName,
            onValueChange = {
                settings.copyrightTagsCategoryName = it
                Properties.saveSettings()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
        )

        TextAndTextField(
            name = "Artists category:",
            value = settings.artistTagsCategoryName,
            onValueChange = {
                settings.artistTagsCategoryName = it
                Properties.saveSettings()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
        )

        TextAndTextField(
            name = "Meta category:",
            value = settings.metaTagsCategoryName,
            onValueChange = {
                settings.metaTagsCategoryName = it
                Properties.saveSettings()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
        )
    }
}

@Composable
private fun TextAndTextField(
    name: String,
    value: String,
    onValueChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(name, color = colorText, fontSize = normalText)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
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
                    color = colorBackground,
                    shape = RoundedCornerShape(smallCorners)
                )
                .padding(padding),
        )
    }
}

@Composable
private fun Backup(
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier,
    ) {
        var creatingBackup by remember { mutableStateOf(false) }
        var deletingBackup by remember { mutableStateOf(false) }

        Text("Backup:", color = colorText, fontSize = bigText)

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ButtonText(
                text = "Create backup",
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        creatingBackup = true
                        Properties.backup.createBackup()
                        creatingBackup = false
                    }
                },
                enabled = !creatingBackup && !deletingBackup,
                modifier = Modifier.padding(padding),
            )

            Text(
                text = "Backup limit:",
                color = colorText,
                fontSize = normalText,
                modifier = Modifier.padding(padding),
            )

            Menu(
                selectedItem = settings.backupLimit,
                items = listOf(1, 2, 5, 10, 25),
                onSelect = {
                    settings.backupLimit = it as Int
                    Properties.saveSettings()
                    Properties.backup.limitUpdated(settings.backupLimit)
                },
                itemContent = { item, type ->
                    Text(
                        (item as Int).toString(),
                        color = if (type == MenuItemType.ListSelected) colorTextSecond else colorText,
                        fontSize = normalText,
                        modifier = Modifier
                            .background(colorBackground, RoundedCornerShape(corners))
                            .padding(top = padding, bottom = padding, start = biggerPadding, end = biggerPadding),
                    )
                },
                modifier = Modifier.padding(padding),
            )

            if (creatingBackup) {
                Text(
                    text = "Creating backup...",
                    color = colorText,
                    fontSize = normalText,
                    modifier = Modifier.padding(padding),
                )
            }

            if (deletingBackup) {
                Text(
                    text = "Deleting backup...",
                    color = colorText,
                    fontSize = normalText,
                    modifier = Modifier.padding(padding),
                )
            }
        }

        if (Properties.backup.backups.isEmpty()) {
            Text(
                text = "No backups found",
                color = colorText,
                fontSize = normalText,
                modifier = Modifier.padding(padding),
            )
        }

        Properties.backup.backups.forEach { info ->
            BackupInfoCard(
                info = info,
                onRemove = {
                    coroutineScope.launch(Dispatchers.IO) {
                        deletingBackup = true
                        Properties.backup.removeBackup(info)
                        deletingBackup = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.5F)
                    .padding(padding),
            )
        }
    }
}

@Composable
private fun BackupInfoCard(
    info: BackupInfo,
    onRemove: () -> Unit,
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
        Row {
            Text("id: ${info.id} | ", color = colorText, fontSize = normalText)
            Text("${info.date.toTimeString()} | ", color = colorText, fontSize = normalText)
            Text("${info.imageCount} images | ", color = colorText, fontSize = normalText)
            Text("${(info.spaceUsed / 1024.0 / 1024.0).roundPlaces(2)} MB", color = colorText, fontSize = normalText)
        }

        Row {
            Icon(
                painter = painterResource("folder_open.svg"),
                contentDescription = null,
                tint = colorText,
                modifier = Modifier
                    .size(iconSize)
                    .clickable { "explorer \"${File(info.folderPath).absolutePath}\\\"".runCommand(File(".")) }
                    .padding(horizontal = biggerPadding, vertical = padding),
            )

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