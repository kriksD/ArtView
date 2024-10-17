package info.media.serializers

import info.media.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

object MediaInfoSerializer : JsonContentPolymorphicSerializer<MediaInfo>(MediaInfo::class) {
    override fun selectDeserializer(element: JsonElement): KSerializer<out MediaInfo> {
        val type = element.jsonObject["type"]?.jsonPrimitive?.content?.let { MediaType.valueOf(it) }
            ?: element.jsonObject["path"]?.jsonPrimitive?.content?.let { MediaType.determineType(File(it)) }

        return when (type) {
            MediaType.Image -> ImageInfo.serializer()
            MediaType.GIF -> GIFInfo.serializer()
            MediaType.Video -> VideoInfo.serializer()
            MediaType.Audio -> AudioInfo.serializer()
            null -> ImageInfo.serializer()
        }
    }
}