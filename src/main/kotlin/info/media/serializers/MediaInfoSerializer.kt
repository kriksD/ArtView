package info.media.serializers

import info.media.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object MediaInfoSerializer : JsonContentPolymorphicSerializer<MediaInfo>(MediaInfo::class) {
    override fun selectDeserializer(element: JsonElement) = when(MediaType.valueOf(element.jsonObject["type"]?.jsonPrimitive?.content ?: throw SerializationException("Missing type"))) {
        MediaType.Image -> ImageInfo.serializer()
        MediaType.GIF -> GIFInfo.serializer()
        MediaType.Video -> VideoInfo.serializer()
        MediaType.Audio -> AudioInfo.serializer()
    }
}