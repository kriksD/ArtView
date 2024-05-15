package booruClient.parser

import booruClient.BooruPost
import booruClient.ImageLoaderClient
import booruClient.splitTags
import kotlinx.serialization.json.*

class SafebooruPostJSONParser : BooruPostJSONParser {
    override suspend fun parseJson(json: String, link: String): BooruPost? {
        val decodedJson = Json.parseToJsonElement(json).jsonArray
        val jsonMap = decodedJson.firstOrNull()?.jsonObject?.toMap() ?: return null

        val imageName = jsonMap["image"]?.jsonPrimitive?.content ?: return null
        val imageDirectory = jsonMap["directory"]?.jsonPrimitive?.content ?: return null
        val baseUrl = link.substringBefore("/index.php?")
        val imageUrl = "$baseUrl//images/$imageDirectory/$imageName"
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