package composableFunctions

import ImageInfo
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import bigIconSize
import colorBackground
import colorBackgroundLighter
import colorBackgroundSecondLighter
import colorText
import corners
import emptyImageBitmap
import hugeText
import iconSize
import normalAnimationDuration
import normalText
import padding
import properties.Properties
import shortAnimationDuration
import transparency
import transparencySecond

@Composable
fun ImagePreview(
    selectedImage: ImageInfo?,
    onClose: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Crossfade(
        selectedImage,
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
                        fontSize = hugeText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorBackground.copy(alpha = transparency))
                            .padding(padding),
                    )

                    Image(
                        imgData.image ?: emptyImageBitmap,
                        imgData.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1F)
                    )

                    var expanded by remember { mutableStateOf(false) }
                    BasicTagTable(
                        tags = imgData.tags,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
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
                                        Properties.imagesData().images.find { it.path == imgData.path }?.favorite =
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
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "next image",
                    tint = colorText,
                    modifier = Modifier
                        .size(bigIconSize)
                        .align(Alignment.CenterEnd)
                        .clickable(onClick = onNext)
                )

                Icon(
                    Icons.Default.KeyboardArrowLeft,
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