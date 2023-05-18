// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import properties.Properties
import java.io.File
import java.net.URI

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    Properties.loadStyle()
    Properties.loadSettings()
    Properties.loadLanguage(settings.language)
    Properties.loadData()

    val images = remember { Properties.imagesData().images.toState() }
    var filteredImages by remember { mutableStateOf(images.toList()) }
    val selectedImages = remember { mutableStateListOf<ImageInfo>() }
    var selectedImage by remember { mutableStateOf<ImageInfo?>(null) }

    val tags = remember { Properties.imagesData().tags.toState() }
    val selectedTags = remember { mutableStateListOf<String>() }
    val antiSelectedTags = remember { mutableStateListOf<String>() }

    var isFirstTime by remember { mutableStateOf(true) }
    if (isFirstTime) {
        isFirstTime = false

        File("images").mkdir()
        antiSelectedTags.add("NSFW")
        filteredImages = images.filter { image -> antiSelectedTags.none { tag -> image.tags.contains(tag) } }
    }

    fun newTag(tag: String) {
        if (tag.isNotEmpty() && !selectedTags.contains(tag)) {
            tags.add(tag)
            tags.sort()
            Properties.imagesData().tags.add(tag)
            Properties.imagesData().tags.sort()
            Properties.saveData()
        }
    }

    val windowState = rememberWindowState(
        width = 1400.dp,
        height = 700.dp,
        position = WindowPosition.Aligned(Alignment.Center)
    )

    Window(onCloseRequest = ::exitApplication, state = windowState) {
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
                                    addImage(URI(path).path)?.let { images.add(0, it) }
                                }
                            }
                        }
                        Properties.saveData()
                    }
                ),
        ) {
            LeftSideMenu(
                menuItem,
                onOptionSelected = { menuItem = it },
                modifier = Modifier.fillMaxHeight(),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(
                    menuItem,
                    animationSpec = tween(normalAnimationDuration),
                    modifier = Modifier.fillMaxSize(),
                ) { item ->

                    @Composable
                    fun ImageTableView(
                        filter: (ImageInfo) -> Boolean,
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        Column {
                            TagTable(
                                tags = tags,
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
                                onNew = ::newTag,
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colorBackgroundLighter),
                            )

                            ImageGrid(
                                imageInfo = images.filter(filter).also { filteredImages = it },
                                checkedList = selectedImages,
                                onCheckedClick = { imgInfo, isSelected ->
                                    if (isSelected) {
                                        selectedImages.add(imgInfo)
                                    } else {
                                        selectedImages.remove(imgInfo)
                                    }
                                },
                                onOpen = {
                                    if (selectedImages.isEmpty()) {
                                        selectedImage = it
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

                    when (item) {
                        MenuItem.Images -> {
                            ImageTableView(filter = { image ->
                                val hasSelectedTags = selectedTags.all { tag -> image.tags.contains(tag) }
                                val hasAntiSelectedTags = antiSelectedTags.none { tag -> image.tags.contains(tag) }
                                hasSelectedTags && hasAntiSelectedTags
                            })
                        }
                        MenuItem.Favorites -> {
                            ImageTableView(filter = { image ->
                                val hasSelectedTags = selectedTags.all { tag -> image.tags.contains(tag) }
                                val hasAntiSelectedTags = antiSelectedTags.none { tag -> image.tags.contains(tag) }
                                hasSelectedTags && hasAntiSelectedTags && image.favorite
                            })
                        }
                        MenuItem.Collections -> {}
                        MenuItem.Settings -> {}
                    }
                }

                var isEditing by remember { mutableStateOf(false) }
                ImagePreview(
                    selectedImage,
                    onClose = {
                        selectedImage = null
                        isEditing = false
                    },
                    onNext = {
                        filteredImages.getOrNull(filteredImages.indexOf(selectedImage) + 1)
                            ?.let { selectedImage = it }
                    },
                    onPrevious = {
                        filteredImages.getOrNull(filteredImages.indexOf(selectedImage) - 1)
                            ?.let { selectedImage = it }
                    },
                    onDelete = {
                        selectedImage?.let { Properties.imagesData().delete(it) }
                        images.remove(selectedImage)
                        selectedImage = null
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
                    selectedImage?.let { imgInfo ->
                        EditImageWindow(
                            tags = tags,
                            imageInfo = imgInfo,
                            onCancel = { isEditing = false },
                            onDone = { newImageInfo ->
                                val index = images.indexOf(images.find { it.path == newImageInfo.path })
                                images[index] = newImageInfo
                                Properties.imagesData().images[index] = newImageInfo
                                Properties.saveData()
                                selectedImage = newImageInfo
                                isEditing = false
                            },
                            onNewTag = ::newTag,
                            modifier = Modifier
                                .fillMaxWidth(0.65F)
                                .heightIn(0.dp, (window.height * 0.8F).dp)
                                .background(colorBackground.copy(transparencySecond), RoundedCornerShape(corners))
                                .padding(padding),
                        )
                    }
                }

                var isEditingTags by remember { mutableStateOf(false) }
                AppearDisappearAnimation(
                    selectedImages.isNotEmpty(),
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
                                selectedImages.clear()
                                selectedImages.addAll(filteredImages)
                            },
                        )
                        ButtonText(
                            "Deselect all",
                            onClick = { selectedImages.clear() },
                        )
                        ButtonText(
                            "Manage tags for all",
                            onClick = { isEditingTags = true },
                        )
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
                        tags = tags,
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
                        onNewTag = ::newTag,
                        onDone = {
                            images.forEach { ii ->
                                if (selectedImages.contains(ii)) {
                                    ii.tags.addAll(newTags.filter { !ii.tags.contains(it) })
                                    ii.tags.removeAll(removeTags)
                                }
                            }
                            Properties.imagesData().images.forEach { ii ->
                                if (selectedImages.find { fi -> fi.path == ii.path } != null) {
                                    ii.tags.addAll(newTags.filter { !ii.tags.contains(it) })
                                    ii.tags.removeAll(removeTags)
                                }
                            }
                            Properties.saveData()

                            selectedImages.clear()
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
            }
        }
    }
}