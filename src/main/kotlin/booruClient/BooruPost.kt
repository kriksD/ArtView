package booruClient

import androidx.compose.ui.graphics.ImageBitmap

data class BooruPost(
    val image: ImageBitmap,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val title: String? = null,
    val source: String? = null,
    val rating: String? = null,
    val tags: List<String> = listOf(),
    val character: List<String> = listOf(),
    val copyright: List<String> = listOf(),
    val artist: List<String> = listOf(),
    val meta: List<String> = listOf(),
)
