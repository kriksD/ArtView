package composableFunctions

import ImageGroup
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import bigText
import border
import calculateWeight
import colorBackground
import colorBackgroundSecond
import colorBackgroundSecondLighter
import colorText
import colorTextSecond
import emptyImageBitmap
import iconSize
import normalAnimationDuration
import normalText
import padding
import properties.Properties
import scrollbarThickness
import shortAnimationDuration
import smallCorners
import style
import transparencyLight

@Composable
fun ImageGroupGrid(
    imageGroups: List<ImageGroup>,
    checkedList: List<ImageGroup> = listOf(),
    onCheckedClick: (ImageGroup, Boolean) -> Unit = { _, _ -> },
    onOpen: (ImageGroup) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var imagesPerRow by remember { mutableStateOf(8) }
    val splitImages = imageGroups.chunked(imagesPerRow)
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .weight(1F)
                .verticalScroll(scrollState)
                .onSizeChanged {
                    imagesPerRow = it.width / style.image_width
                }
        ) {
            splitImages.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    row.forEach { imgGroup ->
                        ImageGroupGridItem(
                            imageGroup = imgGroup,
                            checked = checkedList.contains(imgGroup),
                            onCheckedChange = { onCheckedClick(imgGroup, it) },
                            onOpen = { onOpen(imgGroup) },
                            modifier = Modifier
                                .weight(imgGroup.getImageInfo(0)?.calculateWeight() ?: 1F)
                                .padding(padding)
                                .clip(RoundedCornerShape(smallCorners))
                        )
                    }

                    repeat(imagesPerRow - row.size) {
                        Spacer(
                            modifier = Modifier
                                .weight(1F)
                                .padding(padding)
                        )
                    }
                }
            }
        }

        VerticalScrollbar(
            rememberScrollbarAdapter(scrollState),
            style = LocalScrollbarStyle.current.copy(unhoverColor = colorBackgroundSecondLighter, hoverColor = colorBackgroundSecond),
            modifier = Modifier
                .padding(top = padding, end = padding, bottom = padding)
                .background(colorBackground, RoundedCornerShape(smallCorners))
                .fillMaxHeight()
                .width(scrollbarThickness),
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ImageGroupGridItem(
    imageGroup: ImageGroup,
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    onOpen: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showInfo by remember { mutableStateOf(false) }
    var favorite by remember { mutableStateOf(imageGroup.favorite) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .border(border, if (checked) colorBackgroundSecondLighter else Color.Transparent)
            .clickable(onClick = onOpen)
            .onPointerEvent(PointerEventType.Enter) {
                showInfo = true
            }
            .onPointerEvent(PointerEventType.Exit) {
                showInfo = false
            }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Image(
                imageGroup.getImageInfo(1)?.scaledDownImage ?: emptyImageBitmap,
                imageGroup.name,
                modifier = Modifier
                    .fillMaxSize(0.7F)
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(smallCorners)),
            )
            Image(
                imageGroup.getImageInfo(0)?.scaledDownImage ?: emptyImageBitmap,
                imageGroup.name,
                modifier = Modifier
                    .fillMaxSize(0.9F)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(smallCorners)),
            )
        }

        AppearDisappearAnimation(
            showInfo,
            normalAnimationDuration,
        ) {
            Column {
                Text(
                    imageGroup.name,
                    color = colorText,
                    fontSize = bigText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorBackground.copy(alpha = transparencyLight))
                        .padding(padding),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorBackground.copy(alpha = transparencyLight))
                        .padding(padding),
                ) {
                    Text(
                        imageGroup.description,
                        color = colorText,
                        fontSize = normalText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1F)
                    )

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
                                    imageGroup.favorite = favorite
                                    Properties.saveData()
                                }
                        )
                    }

                    Checkbox(
                        checked = checked,
                        onCheckedChange = onCheckedChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = colorTextSecond,
                            uncheckedColor = colorText,
                            checkmarkColor = colorText,
                        )
                    )
                }
            }
        }
    }
}