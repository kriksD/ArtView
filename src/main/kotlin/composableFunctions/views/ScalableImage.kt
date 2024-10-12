package composableFunctions.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import colorText
import emptyImageBitmap
import iconSize
import info.MediaInfo
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
fun ScalableImage(
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

        LaunchedEffect(true) {
            launch(Dispatchers.Default) {
                loadedImage = mediaInfo.image
            }
        }

        Crossfade(
            loadedImage != null,
            animationSpec = tween(normalAnimationDuration),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (it) {
                if (mediaInfo.path.endsWith(".gif")) {
                    var gif by remember { mutableStateOf<AnimatedImage?>(null) }
                    LaunchedEffect(Unit) { gif = loadAnimatedImage(mediaInfo.path) }
                    gif?.let { g ->
                        Image(
                            bitmap = g.animate(),
                            contentDescription = mediaInfo.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(scale)
                                .offset(offset.x.dp, offset.y.dp)
                                .align(Alignment.Center),
                        )
                    }

                } else {
                    Image(
                        bitmap = loadedImage ?: emptyImageBitmap,
                        contentDescription = mediaInfo.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(scale)
                            .offset(offset.x.dp, offset.y.dp)
                            .align(Alignment.Center),
                    )
                }

            } else {
                Box(modifier = Modifier.fillMaxSize())

                LoadingIcon(
                    tint = colorText,
                    contentDescription = "loading an image",
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