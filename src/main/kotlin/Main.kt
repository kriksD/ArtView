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
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import composableFunctions.*
import composableFunctions.screens.GroupGridScreen
import composableFunctions.screens.ImageGridScreen
import composableFunctions.screens.SettingsScreen
import composableFunctions.views.ButtonText
import composableFunctions.window.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import loader.ImageLoader
import properties.Properties
import java.io.File
import java.net.URI

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

    val imageStorage by remember { mutableStateOf(ImageStorage()) }
    val imageLoader by remember { mutableStateOf(ImageLoader()) }

    val tagStorage by remember { mutableStateOf(TagStorage()) }

    var isEditingGroup by remember { mutableStateOf(false) }
    var isAddingImage by remember { mutableStateOf(false) }

    var isFirstTime by remember { mutableStateOf(true) }
    if (isFirstTime) {
        isFirstTime = false

        File("data/images").mkdirs()

        tagStorage.reset()
        imageStorage.filter(Filter().tags(tagStorage))
    }

    LaunchedEffect(imageLoader) {
        imageLoader.load()
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
        var menuItem by remember { mutableStateOf(MenuItem.Images) }
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
                                    Properties.imagesData().addImage(URI(path).path)
                                    imageStorage.update()
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
                            isAddingImage = true
                        }

                        MenuItem.Images -> {
                            imageStorage.setFilter(Filter().tags(tagStorage))
                            imageStorage.withGroups = false
                            imageStorage.update()
                            coroutineScope.launch {
                                delay(normalAnimationDuration.toLong() + 100L)
                                imageLoader.reset()
                            }
                        }

                        MenuItem.Favorites -> {
                            imageStorage.setFilter(
                                Filter().tags(tagStorage).favorite()
                            )
                            imageStorage.withGroups = false
                            imageStorage.update()
                            coroutineScope.launch {
                                delay(normalAnimationDuration.toLong() + 100L)
                                imageLoader.reset()
                            }
                        }

                        MenuItem.Groups -> {
                            imageStorage.setFilter(Filter().tags(tagStorage))
                            imageStorage.withGroups = true
                            imageStorage.update()
                            coroutineScope.launch {
                                delay(normalAnimationDuration.toLong() + 100L)
                                imageLoader.reset()
                            }
                        }

                        MenuItem.Settings -> {
                            imageStorage.reset()
                            coroutineScope.launch {
                                delay(normalAnimationDuration.toLong() + 100L)
                                imageLoader.reset()
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
                        MenuItem.Images -> {
                            ImageGridScreen(
                                imageLoader = imageLoader,
                                imageStorage = imageStorage,
                                tagStorage = tagStorage,
                            )
                        }
                        MenuItem.Favorites -> {
                            ImageGridScreen(
                                imageLoader = imageLoader,
                                imageStorage = imageStorage,
                                tagStorage = tagStorage,
                            )
                        }
                        MenuItem.Groups -> {
                            GroupGridScreen(
                                imageLoader = imageLoader,
                                imageStorage = imageStorage,
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
                    openedImage = imageStorage.openedImage,
                    onClose = {
                        imageStorage.close()
                        isEditing = false
                    },
                    onNext = { imageStorage.next() },
                    onPrevious = { imageStorage.previous() },
                    onDelete = {
                        imageStorage.delete(listOfNotNull(imageStorage.openedImage))
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
                    imageStorage.openedImage?.let { imgInfo ->
                        EditImageWindow(
                            imageInfo = imgInfo,
                            onCancel = { isEditing = false },
                            onDone = { newImageInfo ->
                                val index = Properties.imagesData().images.indexOf(Properties.imagesData().images.find { it.path == newImageInfo.path })
                                Properties.imagesData().images[index] = newImageInfo
                                Properties.saveData()
                                imageStorage.update(true)
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
                    imageStorage.openedImageGroup?.let { imgGroup ->
                        EditImageGroupWindow(
                            imageGroup = imgGroup,
                            onCancel = { isEditingGroup = false },
                            onDone = { newImageGroup ->
                                imageStorage.openedImageGroup?.name = newImageGroup.name
                                imageStorage.openedImageGroup?.description = newImageGroup.description
                                imageStorage.openedImageGroup?.tags?.clear()
                                imageStorage.openedImageGroup?.tags?.addAll(newImageGroup.tags)
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
                var isAddingImagesToGroup by remember { mutableStateOf(false) }
                AppearDisappearAnimation(
                    imageStorage.selectedImages.isNotEmpty() || imageStorage.selectedGroups.isNotEmpty(),
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
                                if (imageStorage.selectedImages.isNotEmpty()) {
                                    imageStorage.selectAll()

                                } else if (imageStorage.selectedGroups.isNotEmpty()) {
                                    imageStorage.selectAllGroups()
                                }
                            },
                        )
                        ButtonText(
                            "Deselect all",
                            onClick = {
                                imageStorage.deselectAll()
                                imageStorage.deselectAllGroups()
                            },
                        )
                        ButtonText(
                            "Delete selected images/groups",
                            onClick = { toDelete = true },
                        )
                        ButtonText(
                            "Save selected images in new folder",
                            onClick = { imageStorage.saveImageFilesTo() },
                        )
                        ButtonText(
                            "Manage tags for all",
                            onClick = { isEditingTags = true },
                        )
                        if (imageStorage.selectedImages.isNotEmpty()) {
                            ButtonText(
                                "Create group",
                                onClick = { imageStorage.createNewGroup() },
                            )
                            ButtonText(
                                "Add to group",
                                onClick = {
                                    Properties.imagesData().imageGroups.forEach { ig ->
                                        ig.imagePaths.firstOrNull()?.let { path ->
                                            Properties.imagesData().images.find { it.path == path }?.let {
                                                imageLoader.loadNext(it)
                                            }
                                        }
                                    }
                                    isAddingImagesToGroup = true
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
                            "You are going to delete these images/groups forever. Are you sure?",
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
                                    if (imageStorage.selectedImages.isNotEmpty()) {
                                        imageStorage.delete(imageStorage.selectedImages)

                                    } else if (imageStorage.selectedGroups.isNotEmpty()) {
                                        imageStorage.deleteGroups(imageStorage.selectedGroups)
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
                            if (imageStorage.selectedImages.isNotEmpty()) {
                                imageStorage.selectedImages.forEach { ii ->
                                    ii.tags.addAll(newTags.filter { !ii.tags.contains(it) })
                                    ii.tags.removeAll(removeTags)
                                }

                            } else if (imageStorage.selectedGroups.isNotEmpty()) {
                                imageStorage.selectedGroups.forEach { ig ->
                                    ig.tags.addAll(newTags.filter { !ig.tags.contains(it) })
                                    ig.tags.removeAll(removeTags)
                                }
                            }

                            Properties.saveData()
                            imageStorage.update()
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
                    isAddingImagesToGroup,
                    normalAnimationDuration,
                    Modifier.align(Alignment.Center),
                ) {
                    ImageGroupListWindow(
                        onDone = { imageGroup ->
                            imageGroup.imagePaths.addAll(imageStorage.selectedImages.map { it.path }.filter { !imageGroup.imagePaths.contains(it) })

                            Properties.saveData()
                            imageStorage.update()

                            isAddingImagesToGroup = false
                        },
                        onCancel = {
                            isAddingImagesToGroup = false
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
                    isAddingImage,
                    normalAnimationDuration,
                    Modifier.align(Alignment.Center),
                ) {
                    AddWindow(
                        onUrl = { url ->
                            addImageURL = url
                            isAddingImage = false
                        },
                        onCancel = {
                            isAddingImage = false
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
                            imageStorage.update()

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
                        imageStorage = imageStorage,
                        imageLoader = imageLoader,
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
    imageStorage: ImageStorage,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
) {
    var showDebug by remember { mutableStateOf(false) }
    if (showDebug) {
        Column(
            modifier = modifier.background(colorBackground.copy(transparencySecond)),
        ) {
            Text(
                "The size of text: ${Properties.imagesData().countSize()} bytes (${Properties.imagesData().countSize() / 1024} KB)",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "The size of images: ${Properties.imagesData().countImageSize() / 1024} KB (${Properties.imagesData().countImageSize() / 1024 / 1024} MB)",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Loaded images: ${Properties.imagesData().images.size}/${Properties.imagesData().images.count { it.isLoaded }}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Filtered images: ${Properties.imagesData().images.size}/${imageStorage.filteredImages.size}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Selected images: ${imageStorage.filteredImages.size}/${imageStorage.selectedImages.size}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Filtered groups: ${Properties.imagesData().imageGroups.size}/${imageStorage.filteredGroups.size}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Selected groups: ${imageStorage.filteredGroups.size}/${imageStorage.selectedGroups.size}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Is loading: ${imageLoader.isLoading}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Loading requests: ${imageLoader.requestAmount}",
                color = colorText,
                fontSize = normalText,
            )
            Text(
                "Threads used: ${imageLoader.threadCount}",
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