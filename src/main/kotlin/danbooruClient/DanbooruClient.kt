package danbooruClient

import capitalizeEachWord
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.*

object DanbooruClient {
    private val client = HttpClient(OkHttp) {
        install(HttpTimeout)
    }

    suspend fun loadPost(link: String): DanbooruPost? {
        return try {
            val result = client.get(createLink(link)) {
                timeout {
                    requestTimeoutMillis = 20_000
                    socketTimeoutMillis = 20_000
                }
            }

            val jsonMap = Json.parseToJsonElement(result.bodyAsText()).jsonObject.toMap()

            val imageUrl = jsonMap["file_url"]?.jsonPrimitive?.content ?: return null
            val image = ImageLoaderClient().loadImageBitmap(imageUrl) ?: return null

            val imageWidth = jsonMap["image_width"]?.jsonPrimitive?.intOrNull ?: image.width
            val imageHeight = jsonMap["image_height"]?.jsonPrimitive?.intOrNull ?: image.height

            val tags = jsonMap["tag_string_general"]?.jsonPrimitive?.content ?: ""
            val character = jsonMap["tag_string_character"]?.jsonPrimitive?.content ?: ""
            val copyright = jsonMap["tag_string_copyright"]?.jsonPrimitive?.content ?: ""
            val artist = jsonMap["tag_string_artist"]?.jsonPrimitive?.content ?: ""
            val meta = jsonMap["tag_string_meta"]?.jsonPrimitive?.content ?: ""

            DanbooruPost(
                image = image,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                tags = splitTags("$tags $meta"),
                character = splitTags(character, capitalize = true),
                copyright = splitTags(copyright, capitalize = true),
                artist = splitTags(artist),
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createLink(link: String) = if (link.endsWith(".json")) link else "$link.json"

    private fun splitTags(tagString: String, capitalize: Boolean = false): List<String> = tagString
        .split(" ")
        .map { it.replace("_", " ") }
        .let { if (capitalize) it.map { tag -> tag.capitalizeEachWord() } else it }
}