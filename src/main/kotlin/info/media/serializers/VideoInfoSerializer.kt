package info.media.serializers

import info.media.MediaType
import info.media.VideoInfo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

class VideoInfoSerializer : KSerializer<VideoInfo> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("VideoInfo") {
        element<String>("type")
        element<Int>("id")
        element<String>("path")
        element<String>("name")
        element<String>("description")
        element<Boolean>("favorite")
        element<List<String>>("tags")
        element<String?>("source")
        element<String?>("rating")
        element<Int>("width")
        element<Int>("height")
        element<Long>("duration")
        element<Int>("thumbnail_frame")
        element<Int>("thumbnail_id")
        element<Int>("thumbnail_width")
        element<Int>("thumbnail_height")
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): VideoInfo = decoder.decodeStructure(descriptor) {
        var id: Int? = null
        var path: String? = null
        var name: String? = null
        var description: String? = null
        var favorite: Boolean? = null
        var tags: List<String>? = null
        var source: String? = null
        var rating: String? = null
        var width: Int? = null
        var height: Int? = null
        var duration: Long? = null
        var thumbnailFrame: Int? = null
        var thumbnailId: Int? = null
        var thumbnailWidth: Int? = null
        var thumbnailHeight: Int? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> continue
                1 -> id = decodeIntElement(descriptor, index)
                2 -> path = decodeStringElement(descriptor, index)
                3 -> name = decodeStringElement(descriptor, index)
                4 -> description = decodeStringElement(descriptor, index)
                5 -> favorite = decodeBooleanElement(descriptor, index)
                6 -> tags = decodeSerializableElement(descriptor, index, ListSerializer(String.serializer()))
                7 -> source = decodeNullableSerializableElement(descriptor, index, String.serializer())
                8 -> rating = decodeNullableSerializableElement(descriptor, index, String.serializer())
                9 -> width = decodeIntElement(descriptor, index)
                10 -> height = decodeIntElement(descriptor, index)
                11 -> duration = decodeLongElement(descriptor, index)
                12 -> thumbnailFrame = decodeIntElement(descriptor, index)
                13 -> thumbnailId = decodeNullableSerializableElement(descriptor, index, Int.serializer())
                14 -> thumbnailWidth = decodeNullableSerializableElement(descriptor, index, Int.serializer())
                15 -> thumbnailHeight = decodeNullableSerializableElement(descriptor, index, Int.serializer())
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        require(path != null)
        require(width != null)
        require(height != null)
        require(duration != null)

        return@decodeStructure VideoInfo(
            id = id ?: -1,
            path = path,
            name = name ?: "",
            description = description ?: "",
            favorite = favorite ?: false,
            tags = tags?.toMutableList() ?: mutableListOf(),
            source = source,
            rating = rating,
            width = width,
            height = height,
            duration = duration,
            thumbnailFrame = thumbnailFrame ?: 0,
            thumbnailID = thumbnailId,
            thumbnailWidth = thumbnailWidth,
            thumbnailHeight = thumbnailHeight,
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: VideoInfo) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, MediaType.Video.toString())
        encodeIntElement(descriptor, 1, value.id)
        encodeStringElement(descriptor, 2, value.path)
        encodeStringElement(descriptor, 3, value.name)
        encodeStringElement(descriptor, 4, value.description)
        encodeBooleanElement(descriptor, 5, value.favorite)
        encodeSerializableElement(descriptor, 6, ListSerializer(String.serializer()), value.tags)
        encodeNullableSerializableElement(descriptor, 7, String.serializer(), value.source)
        encodeNullableSerializableElement(descriptor, 8, String.serializer(), value.rating)
        encodeIntElement(descriptor, 9, value.width)
        encodeIntElement(descriptor, 10, value.height)
        encodeLongElement(descriptor, 11, value.duration)
        encodeIntElement(descriptor, 12, value.thumbnailFrame)
        encodeNullableSerializableElement(descriptor, 13, Int.serializer(), value.thumbnailID)
        encodeNullableSerializableElement(descriptor, 14, Int.serializer(), value.thumbnailWidth)
        encodeNullableSerializableElement(descriptor, 15, Int.serializer(), value.thumbnailHeight)
    }
}