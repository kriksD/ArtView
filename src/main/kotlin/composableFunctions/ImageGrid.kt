package composableFunctions

import loader.ThumbnailLoader
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bigIconSize
import biggerPadding
import border
import colorBackground
import colorBackgroundSecond
import colorBackgroundSecondLighter
import colorText
import colorTextSecond
import composableFunctions.views.LoadingImage
import formatTime
import iconSize
import info.media.*
import normalAnimationDuration
import normalText
import padding
import properties.Properties
import scrollbarThickness
import shortAnimationDuration
import smallCorners
import smallText
import style
import transparencyLight

@Composable
fun ImageGrid(
    mediaList: List<MediaInfo>,
    thumbnailLoader: ThumbnailLoader,
    checkedList: List<MediaInfo> = listOf(),
    onCheckedClick: (MediaInfo, Boolean) -> Unit = { _, _ -> },
    onOpen: (MediaInfo) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var mediaPerRow by remember { mutableStateOf(4) }
    val splitMedia = mediaList.chunked(mediaPerRow).toMutableStateList()
    val scrollState = rememberLazyListState()

    LaunchedEffect(scrollState.layoutInfo.visibleItemsInfo) {
        splitMedia.forEachIndexed { index, row ->
            val isVisible = scrollState.layoutInfo.visibleItemsInfo.find { it.index == index } != null

            if (isVisible) {
                row.forEach { thumbnailLoader.loadNext(it) }
            } else {
                row.forEach { thumbnailLoader.unloadNext(it) }
            }
        }
    }

    Row(
        modifier = modifier,
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .weight(1F)
                .onSizeChanged {
                    val previousMediaPerRow = mediaPerRow
                    mediaPerRow = it.width / style.image_width

                    if (mediaPerRow != previousMediaPerRow) {
                        splitMedia.clear()
                        splitMedia.addAll(mediaList.chunked(mediaPerRow))
                    }
                },
        ) {
            items(splitMedia) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    row.forEach { mediaInfo ->
                        ImageGridItem(
                            mediaInfo = mediaInfo,
                            checked = checkedList.contains(mediaInfo),
                            onCheckedChange = { onCheckedClick(mediaInfo, it) },
                            onOpen = { onOpen(mediaInfo) },
                            modifier = Modifier
                                .weight(mediaInfo.thumbnailWeight())
                                .padding(padding)
                                .clip(RoundedCornerShape(smallCorners)),
                        )
                    }

                    repeat(mediaPerRow - row.size) {
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
            style = LocalScrollbarStyle.current.copy(
                unhoverColor = colorBackgroundSecondLighter,
                hoverColor = colorBackgroundSecond
            ),
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
private fun ImageGridItem(
    mediaInfo: MediaInfo,
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    onOpen: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showInfo by remember(mediaInfo) { mutableStateOf(false) }
    var favorite by remember(mediaInfo) { mutableStateOf(mediaInfo.favorite) }

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
        LoadingImage(
            mediaInfo = mediaInfo,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(smallCorners))
        )

        if (mediaInfo is AudioInfo && mediaInfo.thumbnailWidth == null && mediaInfo.thumbnailHeight == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1F)
                    .background(colorBackground),
            )

            Icon(
                painter = painterResource("volume_up.svg"),
                contentDescription = null,
                tint = colorText,
                modifier = Modifier
                    .background(colorBackground)
                    .padding(biggerPadding)
                    .size(bigIconSize)
                    .align(Alignment.Center),
            )
        }

        AppearDisappearAnimation(
            !showInfo,
            normalAnimationDuration,
        ) {
            val iconMediaType by remember(mediaInfo) {
                mutableStateOf(
                    when (mediaInfo.type) {
                        MediaType.GIF -> "gif_box.svg"
                        MediaType.Video -> "smart_display.svg"
                        MediaType.Audio -> "music_note.svg"
                        else -> null
                    }
                )
            }

            val duration by remember(mediaInfo) {
                mutableStateOf(
                    when (mediaInfo.type) {
                        MediaType.GIF -> null
                        MediaType.Video -> (mediaInfo as VideoInfo).duration
                        MediaType.Audio -> (mediaInfo as AudioInfo).duration
                        else -> null
                    }
                )
            }

            Column {
                iconMediaType?.let {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = null,
                        tint = colorText,
                        modifier = Modifier
                            .size(iconSize)
                            .padding(2.dp)
                            .background(
                                colorBackground.copy(alpha = transparencyLight),
                                RoundedCornerShape(smallCorners)
                            ),
                    )
                }

                duration?.let {
                    Text(
                        it.formatTime(),
                        color = colorText,
                        fontSize = smallText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .background(colorBackground.copy(alpha = transparencyLight), RoundedCornerShape(smallCorners))
                            .padding(2.dp),
                    )
                }
            }
        }

        AppearDisappearAnimation(
            showInfo,
            normalAnimationDuration,
        ) {
            Column {
                Text(
                    mediaInfo.name,
                    color = colorText,
                    fontSize = normalText,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
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
                        mediaInfo.description,
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
                                    mediaInfo.favorite = favorite
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