import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import properties.Properties

@Composable
fun ImageGrid(
    imageData: List<ImageInfo>,
    onOpen: (ImageInfo) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var imagesPerRow by remember { mutableStateOf(8) }
    val splitImages = imageData.chunked(imagesPerRow)

    LazyColumn(
        modifier = modifier
            .onSizeChanged {
                imagesPerRow = it.width / style.image_width
            }
    ) {
        items(splitImages) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { imgData ->
                    ImageGridItem(
                        imgData,
                        onOpen = { onOpen(imgData) },
                        modifier = Modifier
                            .weight(imgData.calculateWeight())
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
}

private fun ImageInfo.calculateWeight(): Float = (image?.width?.toFloat() ?: 1F) / (image?.height?.toFloat() ?: 1F)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ImageGridItem(
    imageInfo: ImageInfo,
    onOpen: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showInfo by remember { mutableStateOf(false) }
    var favorite by remember { mutableStateOf(imageInfo.favorite) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onOpen)
            .onPointerEvent(PointerEventType.Enter) {
                showInfo = true
            }
            .onPointerEvent(PointerEventType.Exit) {
                showInfo = false
            }
    ) {
        Image(
            imageInfo.image ?: emptyImageBitmap,
            imageInfo.name,
            modifier = Modifier.fillMaxSize(),
        )

        AppearDisappearAnimation(
            showInfo,
            normalAnimationDuration,
        ) {
            Column {
                Text(
                    imageInfo.name,
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
                        imageInfo.description,
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
                                    imageInfo.favorite = favorite
                                    Properties.imagesData().images.find { it.path == imageInfo.path }?.favorite = favorite
                                    Properties.saveData()
                                }
                        )
                    }
                }
            }
        }
    }
}