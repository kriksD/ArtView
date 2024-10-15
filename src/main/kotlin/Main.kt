// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import composableFunctions.AppearDisappearAnimation
import composableFunctions.ImagePreview
import composableFunctions.LeftSideMenu
import composableFunctions.MenuItem
import composableFunctions.screens.GroupGridScreen
import composableFunctions.screens.ImageGridScreen
import composableFunctions.screens.SettingsScreen
import composableFunctions.views.ButtonText
import composableFunctions.window.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import loader.ThumbnailLoader
import mediaStorage.Filter
import mediaStorage.MediaStorage
import properties.Properties
import tag.TagStorage
import java.net.URI
import java.util.logging.LogManager


@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val coroutineScope = rememberCoroutineScope()

    var isFirstTime1 by remember { mutableStateOf(true) }
    if (isFirstTime1) {
        isFirstTime1 = false

        Properties.loadStyle()
        Properties.loadSettings()
        Properties.loadLanguage(settings.language)
        Properties.loadData()
        Properties.backup.load()
    }

    val mediaStorage by remember { mutableStateOf(MediaStorage()) }
    val thumbnailLoader by remember { mutableStateOf(ThumbnailLoader()) }

    val tagStorage by remember { mutableStateOf(TagStorage()) }

    var isEditingGroup by remember { mutableStateOf(false) }
    var isAddingMedia by remember { mutableStateOf(false) }

    var isFirstTime by remember { mutableStateOf(true) }
    if (isFirstTime) {
        isFirstTime = false

        LogManager.getLogManager().reset()

        tagStorage.reset()
        mediaStorage.filter(Filter().tags(tagStorage))
    }

    LaunchedEffect(thumbnailLoader) {
        thumbnailLoader.load()
    }

    val windowState = rememberWindowState(
        width = 1400.dp,
        height = 700.dp,
        position = WindowPosition.Aligned(Alignment.Center)
    )

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Art View ${Properties.VERSION} 🎨🎀💝💖",
    ) {
        var menuItem by remember { mutableStateOf(MenuItem.All) }
        var isDroppable by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .background(colorBackgroundLighter)
                .onExternalDrag(
                    onDragStart = { externalDragValue ->
                        isDroppable = externalDragValue.dragData is DragData.FilesList
                    },
                    onDragExit = {
                        isDroppable = false
                    },
                    onDrop = { externalDragValue ->
                        isDroppable = false
                        when (val dragData = externalDragValue.dragData) {
                            is DragData.FilesList -> {
                                val paths = dragData.readFiles()
                                paths.forEach { path ->
                                    mediaData.addMedia(URI(path).path)
                                    mediaStorage.update()
                                }
                            }
                        }
                        Properties.saveData()
                    }
                ),
        ) {
            LeftSideMenu(
                menuItem,
                onOptionSelected = {
                    if (it != MenuItem.Add) menuItem = it
                    when (it) {
                        MenuItem.Add -> {
                            isAddingMedia = true
                        }

                        MenuItem.All -> {
                            mediaStorage.setFilter(Filter().tags(tagStorage))
                            mediaStorage.withGroups = false
                            mediaStorage.update()
                            coroutineScope.launch {
                                delay(normalAnimationDuration.toLong() + 100L)
                                thumbnailLoader.reset()
                            }
                        }

                        MenuItem.Favorites -> {
                            mediaStorage.setFilter(
                                Filter().tags(tagStorage).favorite()
                            )
                            mediaStorage.withGroups = false
                            mediaStorage.update()
                            coroutineScope.launch {
                                delay(normalAnimationDuration.toLong() + 100L)
                                thumbnailLoader.reset()
                            }
                        }

                        MenuItem.Groups -> {
                            mediaStorage.setFilter(Filter().tags(tagStorage))
                            mediaStorage.withGroups = true
                            mediaStorage.update()
                            coroutineScope.launch {
                                delay(normalAnimationDuration.toLong() + 100L)
                                thumbnailLoader.reset()
                            }
                        }

                        MenuItem.Settings -> {
                            mediaStorage.reset()
                            coroutineScope.launch {
                                delay(normalAnimationDuration.toLong() + 100L)
                                thumbnailLoader.reset()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxHeight(),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(
                    menuItem,
                    animationSpec = tween(normalAnimationDuration),
                    modifier = Modifier.fillMaxSize(),
                ) { item ->
                    when (item) {
                        MenuItem.All -> {
                            ImageGridScreen(
                                thumbnailLoader = thumbnailLoader,
                                mediaStorage = mediaStorage,
                                tagStorage = tagStorage,
                            )
                        }
                        MenuItem.Favorites -> {
                            ImageGridScreen(
                                thumbnailLoader = thumbnailLoader,
                                mediaStorage = mediaStorage,
                                tagStorage = tagStorage,
                            )
                        }
                        MenuItem.Groups -> {
                            GroupGridScreen(
                                thumbnailLoader = thumbnailLoader,
                                mediaStorage = mediaStorage,
                                tagStorage = tagStorage,
                                onGroupEdit = { isEditingGroup = true }
                            )
                        }
                        MenuItem.Settings -> { SettingsScreen(modifier = Modifier.fillMaxWidth()) }
                        else -> {}
                    }
                }

                var isEditing by remember { mutableStateOf(false) }
                ImagePreview(
                    openedMedia = mediaStorage.openedMedia,
                    onClose = {
                        mediaStorage.close()
                        isEditing = false
                    },
                    onNext = { mediaStorage.next() },
                    onPrevious = { mediaStorage.previous() },
                    onDelete = {
                        mediaStorage.delete(listOfNotNull(mediaStorage.openedMedia))
                        isEditing = false
                    },
                    onEdit = { isEditing = true },
                    modifier = Modifier.fillMaxSize(),
                )

                AppearDisappearAnimation(
                    isEditing,
                    normalAnimationDuration,
                    Modifier.align(Alignment.Center),
                ) {
                    mediaStorage.openedMedia?.let { mInfo ->
                        EditImageWindow(
                            mediaInfo = mInfo,
                            onCancel = { isEditing = false },
                            onDone = { newMediaInfo ->
                                val index = mediaData.mediaList.indexOf(mediaData.mediaList.find { it.path == newMediaInfo.path })
                                mediaData.mediaList[index] = newMediaInfo
                                Properties.saveData()
                                mediaStorage.update(true)
                                isEditing = false
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.65F)
                                .heightIn(0.dp, (window.height * 0.8F).dp)
                                .background(colorBackground.copy(transparencySecond), RoundedCornerShape(corners))
                                .padding(padding),
                        )
                    }
                }

                AppearDisappearAnimation(
                    isEditingGroup,
                    normalAnimationDuration,
                    Modifier.align(Alignment.Center),
                ) {
                    mediaStorage.openedMediaGroup?.let { mGroup ->
                        EditImageGroupWindow(
                            mediaGroup = mGroup,
                            onCancel = { isEditingGroup = false },
                            onDone = { newMediaGroup ->
                                mediaStorage.openedMediaGroup?.name = newMediaGroup.name
                                mediaStorage.openedMediaGroup?.description = newMediaGroup.description
                                mediaStorage.openedMediaGroup?.tags?.clear()
                                mediaStorage.openedMediaGroup?.tags?.addAll(newMediaGroup.tags)
                                Properties.saveData()

                                isEditingGroup = false
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.65F)
                                .heightIn(0.dp, (window.height * 0.8F).dp)
                                .background(colorBackground.copy(transparencySecond), RoundedCornerShape(corners))
                                .padding(padding),
                        )
                    }
                }

                var toDelete by remember { mutableStateOf(false) }
                var isEditingTags by remember { mutableStateOf(false) }
                var isAddingMediaToGroup by remember { mutableStateOf(false) }
                AppearDisappearAnimation(
                    mediaStorage.selectedMedia.isNotEmpty() || mediaStorage.selectedGroups.isNotEmpty(),
                    normalAnimationDuration,
                    Modifier
                        .padding(biggerPadding)
                        .align(Alignment.BottomStart),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(padding),
                    ) {
                        ButtonText(
                            "Select all",
                            onClick = {
                                if (mediaStorage.selectedMedia.isNotEmpty()) {
                                    mediaStorage.selectAll()

                                } else if (mediaStorage.selectedGroups.isNotEmpty()) {
                                    mediaStorage.selectAllGroups()
                                }
                            },
                        )
                        ButtonText(
                            "Deselect all",
                            onClick = {
                                mediaStorage.deselectAll()
                                mediaStorage.deselectAllGroups()
                            },
                        )
                        ButtonText(
                            "Delete selected media/groups",
                            onClick = { toDelete = true },
                        )
                        ButtonText(
                            "Save selected media to a new folder",
                            onClick = { mediaStorage.saveMediaFilesTo() },
                        )
                        ButtonText(
                            "Manage tags for all",
                            onClick = { isEditingTags = true },
                        )
                        if (mediaStorage.selectedMedia.isNotEmpty()) {
                            ButtonText(
                                "Create group",
                                onClick = { mediaStorage.createNewGroup() },
                            )
                            ButtonText(
                                "Add to group",
                                onClick = {
                                    mediaData.mediaGroups.forEach { mg ->
                                        mg.getMediaInfo(0)?.let { mediaInfo ->
                                            thumbnailLoader.loadNext(mediaInfo)
                                        }
                                    }
                                    isAddingMediaToGroup = true
                                },
                            )
                        }
                    }
                }

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
                            "You are going to delete these media/groups forever. Are you sure?",
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
                                    if (mediaStorage.selectedMedia.isNotEmpty()) {
                                        mediaStorage.delete(mediaStorage.selectedMedia)

                                    } else if (mediaStorage.selectedGroups.isNotEmpty()) {
                                        mediaStorage.deleteGroups(mediaStorage.selectedGroups)
                                    }

                                    toDelete = false
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = colorBackgroundSecondLighter),
                            ) {
                                Text("Yes", color = colorText, fontSize = normalText)
                            }
                        }
                    }
                }

                AppearDisappearAnimation(
                    isEditingTags,
                    normalAnimationDuration,
                    Modifier.align(Alignment.Center),
                ) {
                    val newTags = remember { mutableStateListOf<String>() }
                    val removeTags = remember { mutableStateListOf<String>() }

                    EditImageTagsWindow(
                        newTags = newTags,
                        removeTags = removeTags,
                        onTagClick = {
                            if (newTags.contains(it)) {
                                newTags.remove(it)
                                removeTags.add(it)

                            } else if (removeTags.contains(it)) {
                                newTags.remove(it)
                                removeTags.remove(it)

                            } else {
                                newTags.add(it)
                                removeTags.remove(it)
                            }
                        },
                        onNew = {
                            if (settings.autoSelectCreatedTags)
                                newTags.add(it)
                        },
                        onDone = {
                            if (mediaStorage.selectedMedia.isNotEmpty()) {
                                mediaStorage.selectedMedia.forEach { ii ->
                                    ii.tags.addAll(newTags.filter { !ii.tags.contains(it) })
                                    ii.tags.removeAll(removeTags)
                                }

                            } else if (mediaStorage.selectedGroups.isNotEmpty()) {
                                mediaStorage.selectedGroups.forEach { ig ->
                                    ig.tags.addAll(newTags.filter { !ig.tags.contains(it) })
                                    ig.tags.removeAll(removeTags)
                                }
                            }

                            Properties.saveData()
                            mediaStorage.update()
                            newTags.clear()
                            removeTags.clear()

                            isEditingTags = false
                        },
                        onCancel = {
                            newTags.clear()
                            removeTags.clear()

                            isEditingTags = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.65F)
                            .heightIn(0.dp, (window.height * 0.8F).dp)
                            .background(colorBackground.copy(transparencySecond), RoundedCornerShape(corners))
                            .padding(padding),
                    )
                }

                AppearDisappearAnimation(
                    isAddingMediaToGroup,
                    normalAnimationDuration,
                    Modifier.align(Alignment.Center),
                ) {
                    ImageGroupListWindow(
                        onDone = { mediaGroup ->
                            mediaGroup.mediaIDs.addAll(mediaStorage.selectedMedia.map { it.id }.filter { !mediaGroup.mediaIDs.contains(it) })

                            Properties.saveData()
                            mediaStorage.update()

                            isAddingMediaToGroup = false
                        },
                        onCancel = {
                            isAddingMediaToGroup = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.65F)
                            .heightIn(0.dp, (window.height * 0.8F).dp)
                            .background(colorBackground.copy(transparencySecond), RoundedCornerShape(corners))
                            .padding(padding),
                    )
                }

                var addImageURL by remember { mutableStateOf<String?>(null) }
                AppearDisappearAnimation(
                    isAddingMedia,
                    normalAnimationDuration,
                    Modifier.align(Alignment.Center),
                ) {
                    AddWindow(
                        onUrl = { url ->
                            addImageURL = url
                            isAddingMedia = false
                        },
                        onCancel = {
                            isAddingMedia = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.65F)
                            .heightIn(0.dp, (window.height * 0.8F).dp)
                            .background(colorBackground.copy(transparencySecond), RoundedCornerShape(corners))
                            .padding(padding),
                    )
                }

                AppearDisappearAnimation(
                    addImageURL != null,
                    normalAnimationDuration,
                    Modifier.align(Alignment.Center),
                ) {
                    AddByURLWindow(
                        url = addImageURL ?: "",
                        onDone = {
                            Properties.saveData()
                            mediaStorage.update()

                            addImageURL = null
                        },
                        onCancel = {
                            addImageURL = null
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.65F)
                            .heightIn(0.dp, (window.height * 0.8F).dp)
                            .background(colorBackground.copy(transparencySecond), RoundedCornerShape(corners))
                            .padding(padding),
                    )
                }

                if (settings.showDebug) {
                    Debug(
                        mediaStorage = mediaStorage,
                        thumbnailLoader = thumbnailLoader,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(biggerPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun Debug(
    mediaStorage: MediaStorage,
    thumbnailLoader: ThumbnailLoader,
    modifier: Modifier = Modifier,
) {
    var showDebug by remember { mutableStateOf(false) }
    if (showDebug) {
        Column(
            modifier = modifier.background(colorBackground.copy(transparencySecond)),
        ) {
            Text(
                "(deprecated) The size of text: ${mediaData.countSize()} bytes (${mediaData.countSize() / 1024} KB)",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "The size of images: ${mediaData.countThumbnailSize() / 1024} KB (${mediaData.countThumbnailSize() / 1024 / 1024} MB)",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Loaded media: ${mediaData.mediaList.size}/${mediaData.mediaList.count { it.isThumbnailLoaded }}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Filtered media: ${mediaData.mediaList.size}/${mediaStorage.filteredMedia.size}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Selected media: ${mediaStorage.filteredMedia.size}/${mediaStorage.selectedMedia.size}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Filtered groups: ${mediaData.mediaGroups.size}/${mediaStorage.filteredGroups.size}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Selected groups: ${mediaStorage.filteredGroups.size}/${mediaStorage.selectedGroups.size}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Is loading: ${thumbnailLoader.isLoading}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Loading requests: ${thumbnailLoader.requestAmount}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Threads used: ${thumbnailLoader.threadCount}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Debug",
                color = colorTextSuccess,
                fontSize = normalText,
                modifier = Modifier.clickable { showDebug = false },
            )
        }
    } else {
        Text(
            "Debug",
            color = colorTextError,
            fontSize = normalText,
            modifier = modifier.clickable { showDebug = true },
        )
    }
}