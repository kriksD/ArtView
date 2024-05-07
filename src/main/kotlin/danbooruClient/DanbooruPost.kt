package danbooruClient

import androidx.compose.ui.graphics.ImageBitmap

data class DanbooruPost(
    val image: ImageBitmap,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val tags: List<String> = listOf(),
    val character: List<String> = listOf(),
    val copyright: List<String> = listOf(),
    val artist: List<String> = listOf(),
)
