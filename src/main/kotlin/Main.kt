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
import composableFunctions.*
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
    //var filteredImages by remember { mutableStateOf(images.toList()) }
    val selectedImages = remember { mutableStateListOf<ImageInfo>() }
    var selectedImage by remember { mutableStateOf<ImageInfo?>(null) }

    val imageLoader by remember { mutableStateOf(ImageLoader()) }

    val groups = remember { Properties.imagesData().imageGroups.toState() }
    var filteredImageGroups by remember { mutableStateOf(groups.toList()) }
    val selectedImageGroups = remember { mutableStateListOf<ImageGroup>() }
    var selectedImageGroup by remember { mutableStateOf<ImageGroup?>(null) }

    val selectedTags = remember { mutableStateListOf<String>() }
    val antiSelectedTags = remember { mutableStateListOf<String>() }

    var isEditingGroup by remember { mutableStateOf(false) }

    var isFirstTime by remember { mutableStateOf(true) }
    if (isFirstTime) {
        isFirstTime = false

        File("images").mkdir()
        antiSelectedTags.addAll(settings.anti_selected_tags_by_default)
        selectedTags.addAll(settings.selected_tags_by_default)

        imageLoader.filter(FilterBuilder().tags(selectedTags).antiTags(antiSelectedTags))
        //filteredImages = imageLoader.loadedList
        /*filteredImages = images.filter { image ->
            val hasSelectedTags = selectedTags.all { tag -> image.tags.contains(tag) }
            val hasAntiSelectedTags = antiSelectedTags.none { tag -> image.tags.contains(tag) }
            hasSelectedTags && hasAntiSelectedTags
        }*/
    }

    val windowState = rememberWindowState(
        width = 1400.dp,
        height = 700.dp,
        position = WindowPosition.Aligned(Alignment.Center)
    )

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Art View ${Properties.version} 🎨🎀💝💖",
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
                                    Properties.imagesData().addImage(URI(path).path)?.let { images.add(0, it) }
                                    imageLoader.update()
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
                    menuItem = it
                    when (menuItem) {
                        MenuItem.Images -> {
                            imageLoader.reset()
                            selectedImageGroups.clear()
                            selectedImageGroup = null
                        }

                        MenuItem.Favorites -> {
                            imageLoader.reset()
                            selectedImageGroups.clear()
                            selectedImageGroup = null
                        }

                        MenuItem.Groups -> {
                            imageLoader.reset()
                            selectedImages.clear()
                            selectedImage = null
                        }

                        MenuItem.Settings -> {
                            imageLoader.reset()
                            selectedImageGroups.clear()
                            selectedImageGroup = null
                            selectedImages.clear()
                            selectedImage = null
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

                    @Composable
                    fun ImageTableView(
                        filter: FilterBuilder,
                    ) {
                        var reload by remember { mutableStateOf(false) }
                        var expanded by remember { mutableStateOf(false) }
                        Column {
                            TagTableWithCategories(
                                tags = Properties.imagesData().tags,
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

                                    imageLoader.reset()
                                    imageLoader.filter(filter.tags(selectedTags).antiTags(antiSelectedTags))
                                    reload = true
                                },
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colorBackgroundLighter),
                            )

                            if (reload) {
                                reload = false
                                return@Column
                            }

                            ImageGrid(
                                imageInfo = imageLoader.also { it.filter(filter) }.loadedList, //images.filter(filter).also { filteredImages = it },
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
                                onEndReached = { amountToLoad ->
                                    repeat(amountToLoad) {
                                        imageLoader.loadNext()
                                    }
                                },
                                modifier = Modifier.weight(1F),
                            )
                        }
                    }

                    when (item) {
                        MenuItem.Images -> {
                            ImageTableView(filter = FilterBuilder().tags(selectedTags).antiTags(antiSelectedTags)
                                /*{ image ->
                                val hasSelectedTags = selectedTags.all { tag -> image.tags.contains(tag) }
                                val hasAntiSelectedTags = antiSelectedTags.none { tag -> image.tags.contains(tag) }
                                hasSelectedTags && hasAntiSelectedTags
                            }*/)
                        }
                        MenuItem.Favorites -> {
                            ImageTableView(filter = FilterBuilder().tags(selectedTags).antiTags(antiSelectedTags).favorite()
                                /*{ image ->
                                val hasSelectedTags = selectedTags.all { tag -> image.tags.contains(tag) }
                                val hasAntiSelectedTags = antiSelectedTags.none { tag -> image.tags.contains(tag) }
                                hasSelectedTags && hasAntiSelectedTags && image.favorite
                            }*/)
                        }
                        MenuItem.Groups -> {
                            if (selectedImageGroup == null) {
                                var expanded by remember { mutableStateOf(false) }
                                Column {
                                    TagTableWithCategories(
                                        tags = Properties.imagesData().tags,
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
                                        expanded = expanded,
                                        onExpandedChange = { expanded = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(colorBackgroundLighter),
                                    )
                                    ImageGroupGrid(
                                        imageGroups = Properties.imagesData().imageGroups.filter { imageGroup ->
                                            val hasSelectedTags =
                                                selectedTags.all { tag -> imageGroup.tags.contains(tag) }
                                            val hasAntiSelectedTags =
                                                antiSelectedTags.none { tag -> imageGroup.tags.contains(tag) }
                                            hasSelectedTags && hasAntiSelectedTags
                                        }.also { filteredImageGroups = it },
                                        checkedList = selectedImageGroups,
                                        onCheckedClick = { imgGroup, isSelected ->
                                            if (isSelected) {
                                                selectedImageGroups.add(imgGroup)
                                            } else {
                                                selectedImageGroups.remove(imgGroup)
                                            }
                                        },
                                        onOpen = {
                                            if (selectedImageGroups.isEmpty()) {
                                                selectedImageGroup = it
                                            } else {
                                                if (selectedImageGroups.contains(it)) {
                                                    selectedImageGroups.remove(it)
                                                } else {
                                                    selectedImageGroups.add(it)
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1F),
                                    )
                                }
                            } else {
                                ImageGroupPreview(
                                    selectedImageGroup!!,
                                    onClose = {
                                        selectedImageGroup = null
                                        selectedImages.clear()
                                    },
                                    onEdit = { isEditingGroup = true },
                                    onSelectedUpdated = { newSelected ->
                                        selectedImages.clear()
                                        selectedImages.addAll(newSelected)
                                    },
                                    onImageSelected = { img, all ->
                                        //filteredImages = all
                                        imageLoader.reset()
                                        FilterBuilder().tags(selectedTags).antiTags(antiSelectedTags).group(selectedImageGroup!!)
                                        selectedImage = img
                                    }
                                )
                            }
                        }
                        MenuItem.Settings -> { SettingsScreen(modifier = Modifier.fillMaxWidth()) }
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
                        imageLoader.loadedList.getOrNull(imageLoader.loadedList.indexOf(selectedImage) + 1)
                            ?.let { selectedImage = it }
                    },
                    onPrevious = {
                        imageLoader.loadedList.getOrNull(imageLoader.loadedList.indexOf(selectedImage) - 1)
                            ?.let { selectedImage = it }
                    },
                    onDelete = {
                        selectedImage?.let {
                            selectedImageGroup?.imagePaths?.remove(it.path)
                            Properties.imagesData().delete(it)
                        }
                        images.remove(selectedImage)
                        Properties.saveData()
                        imageLoader.update()

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
                    selectedImageGroup?.let { imgGroup ->
                        EditImageGroupWindow(
                            imageGroup = imgGroup,
                            onCancel = { isEditingGroup = false },
                            onDone = { newImageGroup ->
                                selectedImageGroup?.name = newImageGroup.name
                                selectedImageGroup?.description = newImageGroup.description
                                selectedImageGroup?.tags?.clear()
                                selectedImageGroup?.tags?.addAll(newImageGroup.tags)
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

                var isEditingTags by remember { mutableStateOf(false) }
                var isAddingImagesToGroup by remember { mutableStateOf(false) }
                AppearDisappearAnimation(
                    selectedImages.isNotEmpty() || selectedImageGroups.isNotEmpty(),
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
                                if (selectedImages.isNotEmpty()) {
                                    selectedImages.clear()
                                    selectedImages.addAll(imageLoader.loadedList)

                                } else if (selectedImageGroups.isNotEmpty()) {
                                    selectedImageGroups.clear()
                                    selectedImageGroups.addAll(filteredImageGroups)
                                }
                            },
                        )
                        ButtonText(
                            "Deselect all",
                            onClick = {
                                selectedImages.clear()
                                selectedImageGroups.clear()
                            },
                        )
                        ButtonText(
                            "Delete selected images/groups",
                            onClick = {
                                if (selectedImages.isNotEmpty()) {
                                    selectedImages.forEach { it.delete() }
                                    images.removeAll(selectedImages)
                                    Properties.imagesData().images.removeAll(selectedImages)
                                    selectedImages.clear()
                                    Properties.saveData()
                                    imageLoader.update()

                                } else if (selectedImageGroups.isNotEmpty()) {
                                    groups.removeAll(selectedImageGroups)
                                    Properties.imagesData().imageGroups.removeAll(selectedImageGroups)
                                    selectedImageGroups.clear()
                                    Properties.saveData()
                                    imageLoader.update()
                                }
                            },
                        )
                        ButtonText(
                            "Save selected images in new folder",
                            onClick = {
                                if (selectedImages.isNotEmpty()) {
                                    val folder = File("images_filtered")
                                    folder.mkdir()
                                    folder.listFiles()?.forEach { it.delete() }
                                    selectedImages.forEach { it.saveFileTo(folder) }

                                } else if (selectedImageGroups.isNotEmpty()) {
                                    val folder = File("images_filtered")
                                    folder.mkdir()
                                    folder.listFiles()?.forEach { it.delete() }
                                    selectedImageGroups.forEach { it.saveImageFilesTo(folder) }
                                }
                            },
                        )
                        ButtonText(
                            "Manage tags for all",
                            onClick = { isEditingTags = true },
                        )
                        if (selectedImages.isNotEmpty()) {
                            ButtonText(
                                "Create group",
                                onClick = {
                                    val imagePaths = selectedImages.map { it.path }.toMutableList()
                                    val newGroup = ImageGroup(imagePaths)

                                    groups.add(newGroup)
                                    Properties.imagesData().imageGroups.add(newGroup)
                                    Properties.saveData()
                                },
                            )
                            ButtonText(
                                "Add to group",
                                onClick = {
                                    isAddingImagesToGroup = true
                                },
                            )
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
                            if (settings.auto_select_created_tags)
                                newTags.add(it)
                        },
                        onDone = {
                            if (selectedImages.isNotEmpty()) {
                                selectedImages.forEach { ii ->
                                    ii.tags.addAll(newTags.filter { !ii.tags.contains(it) })
                                    ii.tags.removeAll(removeTags)
                                }

                            } else if (selectedImageGroups.isNotEmpty()) {
                                selectedImageGroups.forEach { ig ->
                                    ig.tags.addAll(newTags.filter { !ig.tags.contains(it) })
                                    ig.tags.removeAll(removeTags)
                                }
                            }

                            Properties.saveData()
                            selectedImages.clear()
                            selectedImageGroups.clear()
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
                            imageGroup.imagePaths.addAll(selectedImages.map { it.path }.filter { !imageGroup.imagePaths.contains(it) })

                            Properties.saveData()
                            selectedImages.clear()

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
            }
        }
    }
}