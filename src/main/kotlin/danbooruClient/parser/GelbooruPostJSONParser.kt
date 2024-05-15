package danbooruClient.parser

import danbooruClient.BooruPost
import danbooruClient.ImageLoaderClient
import danbooruClient.splitTags
import kotlinx.serialization.json.*

class GelbooruPostJSONParser : BooruPostJSONParser {
    override suspend fun parseJson(json: String, link: String): BooruPost? {
        val decodedJson = Json.parseToJsonElement(json).jsonObject["post"]?.jsonArray ?: return null
        val jsonMap = decodedJson.firstOrNull()?.jsonObject?.toMap() ?: return null

        val imageUrl = jsonMap["file_url"]?.jsonPrimitive?.content ?: return null
        val image = ImageLoaderClient().loadImageBitmap(imageUrl) ?: return null

        val imageWidth = jsonMap["width"]?.jsonPrimitive?.intOrNull ?: image.width
        val imageHeight = jsonMap["height"]?.jsonPrimitive?.intOrNull ?: image.height

        val title = jsonMap["title"]?.jsonPrimitive?.content
        val source = jsonMap["source"]?.jsonPrimitive?.content
        val rating = jsonMap["rating"]?.jsonPrimitive?.content

        val tags = (jsonMap["tags"]?.jsonPrimitive?.content ?: "")

        return BooruPost(
            image = image,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            title = title,
            source = source,
            rating = rating,
            tags = splitTags(tags),
        )
    }
}