package booruClient.parser

import booruClient.BooruPost
import booruClient.ImageLoaderClient
import booruClient.splitTags
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class DanbooruPostJSONParser : BooruPostJSONParser {
    override suspend fun parseJson(json: String, link: String): BooruPost? {
        val jsonMap = Json.parseToJsonElement(json).jsonObject.toMap()

        val imageUrl = jsonMap["file_url"]?.jsonPrimitive?.content ?: return null
        val image = ImageLoaderClient().loadImageBitmap(imageUrl) ?: return null

        val imageWidth = jsonMap["image_width"]?.jsonPrimitive?.intOrNull ?: image.width
        val imageHeight = jsonMap["image_height"]?.jsonPrimitive?.intOrNull ?: image.height

        val title = jsonMap["title"]?.jsonPrimitive?.content
        val source = jsonMap["source"]?.jsonPrimitive?.content
        val rating = jsonMap["rating"]?.jsonPrimitive?.content

        val tags = jsonMap["tag_string_general"]?.jsonPrimitive?.content ?: ""
        val character = jsonMap["tag_string_character"]?.jsonPrimitive?.content ?: ""
        val copyright = jsonMap["tag_string_copyright"]?.jsonPrimitive?.content ?: ""
        val artist = jsonMap["tag_string_artist"]?.jsonPrimitive?.content ?: ""
        val meta = jsonMap["tag_string_meta"]?.jsonPrimitive?.content ?: ""

        return BooruPost(
            image = image,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            title = title,
            source = source,
            rating = rating,
            tags = splitTags(tags),
            character = splitTags(character, capitalize = true),
            copyright = splitTags(copyright, capitalize = true),
            artist = splitTags(artist),
            meta = splitTags(meta),
        )
    }
}