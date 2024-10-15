package composableFunctions

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import bigIconSize
import bigText
import colorBackground
import colorBackgroundLighter
import colorBackgroundSecondLighter
import colorText
import composableFunctions.views.ButtonText
import composableFunctions.views.ScalableMedia
import corners
import iconSize
import info.media.AudioInfo
import info.media.MediaInfo
import info.media.VideoInfo
import mediaData
import normalAnimationDuration
import normalText
import openAudioFile
import openVideoFile
import padding
import properties.Properties
import shortAnimationDuration
import transparency
import transparencySecond

@Composable
fun ImagePreview(
    openedMedia: MediaInfo?,
    onClose: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Crossfade(
        openedMedia,
        animationSpec = tween(normalAnimationDuration),
        modifier = modifier,
    ) { imgData ->
        if (imgData != null) {
            Box(
                modifier = Modifier
                    .background(colorBackgroundLighter.copy(transparencySecond))
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5F)
                        .align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        imgData.name,
                        color = colorText,
                        fontSize = bigText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(padding)
                            .alpha(0.0F),
                    )

                    var imageScale by remember { mutableStateOf(1F) }
                    var imageOffset by remember { mutableStateOf(IntOffset(0, 0)) }
                    ScalableMedia(
                        mediaInfo = imgData,
                        scale = imageScale,
                        onScaleChange = { imageScale = it },
                        offset = imageOffset,
                        onOffsetChange = { imageOffset = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1F),
                    )

                    if (openedMedia is VideoInfo) {
                        ButtonText(
                            "Open this video in a media player",
                            onClick = { openVideoFile(imgData.path) },
                            modifier = Modifier,
                        )
                    } else if (openedMedia is AudioInfo) {
                        ButtonText(
                            "Open this audio in a media player",
                            onClick = { openAudioFile(imgData.path) },
                            modifier = Modifier,
                        )
                    }

                    var expanded by remember { mutableStateOf(false) }
                    TagGrid(
                        tags = imgData.tags,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        controls = false,
                        clickableTags = false,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorBackground.copy(alpha = transparency))
                            .padding(padding),
                    ) {
                        Text(
                            imgData.description,
                            color = colorText,
                            fontSize = normalText,
                            modifier = Modifier.weight(1F)
                        )

                        if (imgData.source != null) {
                            Icon(
                                painter = painterResource("public.svg"),
                                contentDescription = "open source in browser",
                                tint = colorText,
                                modifier = Modifier
                                    .size(iconSize)
                                    .clickable { imgData.openSource() }
                            )
                        }

                        var favorite by remember { mutableStateOf(imgData.favorite) }
                        Crossfade(
                            favorite,
                            animationSpec = tween(shortAnimationDuration),
                        ) { fav ->
                            Icon(
                                if (fav) painterResource("favorite.svg") else painterResource("favorite_o.svg"),
                                contentDescription = null,
                                tint = colorText,
                                modifier = Modifier
                                    .size(iconSize)
                                    .clickable {
                                        favorite = !favorite
                                        imgData.favorite = favorite
                                        mediaData.mediaList.find { it.path == imgData.path }?.favorite =
                                            imgData.favorite
                                        Properties.saveData()
                                    }
                            )
                        }

                        var toDelete by remember { mutableStateOf(false) }
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "delete image",
                            tint = colorText,
                            modifier = Modifier
                                .size(iconSize)
                                .clickable { toDelete = true }
                        )

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
                                    "You are going to delete this image forever. Are you sure?",
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
                                            onDelete()
                                            toDelete = false
                                        },
                                        colors = ButtonDefaults.buttonColors(backgroundColor = colorBackgroundSecondLighter),
                                    ) {
                                        Text("Yes", color = colorText, fontSize = normalText)
                                    }
                                }
                            }
                        }

                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "edit an image data",
                            tint = colorText,
                            modifier = Modifier
                                .size(iconSize)
                                .clickable(onClick = onEdit)
                        )
                    }
                }

                Text(
                    imgData.name,
                    color = colorText,
                    fontSize = bigText,
                    modifier = Modifier
                        .fillMaxWidth(0.5F)
                        .background(colorBackground.copy(alpha = transparency))
                        .padding(padding)
                        .align(Alignment.TopCenter),
                )

                Icon(
                    Icons.Default.Close,
                    contentDescription = "close opened image",
                    tint = colorText,
                    modifier = Modifier
                        .size(bigIconSize)
                        .align(Alignment.TopEnd)
                        .clickable(onClick = onClose)
                )

                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "next image",
                    tint = colorText,
                    modifier = Modifier
                        .size(bigIconSize)
                        .align(Alignment.CenterEnd)
                        .clickable(onClick = onNext)
                )

                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "previous image",
                    tint = colorText,
                    modifier = Modifier
                        .size(bigIconSize)
                        .align(Alignment.CenterStart)
                        .clickable(onClick = onPrevious)
                )
            }
        }
    }
}