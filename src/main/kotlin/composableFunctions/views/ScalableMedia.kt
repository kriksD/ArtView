package composableFunctions.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import bigIconSize
import biggerPadding
import colorBackground
import colorText
import corners
import emptyImageBitmap
import getFirstFrame
import iconSize
import info.media.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import normalAnimationDuration
import org.jetbrains.compose.animatedimage.AnimatedImage
import org.jetbrains.compose.animatedimage.animate
import org.jetbrains.compose.animatedimage.loadAnimatedImage
import padding
import settings
import tinyText
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun ScalableMedia(
    mediaInfo: MediaInfo,
    scale: Float,
    onScaleChange: (Float) -> Unit,
    offset: IntOffset,
    onOffsetChange: (IntOffset) -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .onPointerEvent(PointerEventType.Scroll) { event ->
                event.changes.firstOrNull()?.scrollDelta?.y?.let {
                    val scaleFactor = if (it >= 2) sqrt(scale / 16F) else sqrt(scale / 32F)
                    val scaleChange = it * 4 * scaleFactor
                    onScaleChange(max(0.5F, min(8F, scale - scaleChange)))
                }
            }
            .onDrag {
                onOffsetChange(IntOffset(offset.x + it.x.toInt(), offset.y + it.y.toInt()))
            }
            .pointerInput(Unit){
                detectTapGestures(
                    onDoubleTap = {
                        onScaleChange(1F)
                        onOffsetChange(IntOffset(0, 0))
                    }
                )
            },
    ) {
        var loadedImage by remember { mutableStateOf<ImageBitmap?>(null) }
        var gif by remember { mutableStateOf<AnimatedImage?>(null) }
        var icon by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(true) {
            launch(Dispatchers.Default) {
                when (mediaInfo) {
                    is ImageInfo -> loadedImage = mediaInfo.image
                    is GIFInfo -> gif = loadAnimatedImage(mediaInfo.path)
                    is VideoInfo -> loadedImage = getFirstFrame(mediaInfo.path)
                    is AudioInfo -> {
                        if (mediaInfo.thumbnailWidth == null || mediaInfo.thumbnailHeight == null) {
                            icon = "volume_up.svg"
                        } else {
                            loadedImage = mediaInfo.cover
                        }
                    }
                }
            }
        }

        Crossfade(
            loadedImage != null || gif != null || icon != null,
            animationSpec = tween(normalAnimationDuration),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (it) {
                if (gif != null) {
                    Image(
                        bitmap = gif?.animate() ?: emptyImageBitmap,
                        contentDescription = mediaInfo.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(scale)
                            .offset(offset.x.dp, offset.y.dp)
                            .align(Alignment.Center),
                    )

                } else if (loadedImage != null) {
                    Image(
                        bitmap = loadedImage ?: emptyImageBitmap,
                        contentDescription = mediaInfo.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(scale)
                            .offset(offset.x.dp, offset.y.dp)
                            .align(Alignment.Center),
                    )
                } else if (icon != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(scale)
                            .offset(offset.x.dp, offset.y.dp)
                            .align(Alignment.Center),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(0.8F)
                                .align(Alignment.Center)
                                .aspectRatio(1F)
                                .background(colorBackground, RoundedCornerShape(corners))
                                .align(Alignment.Center),
                        ) {
                            Icon(
                                painter = painterResource(icon!!),
                                contentDescription = null,
                                tint = colorText,
                                modifier = Modifier
                                    .background(colorBackground)
                                    .padding(biggerPadding)
                                    .size(bigIconSize)
                                    .align(Alignment.Center),
                            )
                        }
                    }
                }

            } else {
                Box(modifier = Modifier.fillMaxSize())

                LoadingIcon(
                    tint = colorText,
                    contentDescription = "loading the media",
                    modifier = Modifier.size(iconSize).align(Alignment.Center),
                )
            }
        }

        if (settings.showDebug && (scale != 1F || offset != IntOffset(0, 0))) {
            Text(
                "Scale: $scale\nOffset: $offset",
                color = colorText,
                fontSize = tinyText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
            )
        }
    }
}