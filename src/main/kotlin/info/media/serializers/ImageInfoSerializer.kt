package info.media.serializers

import info.media.ImageInfo
import info.media.MediaType
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

class ImageInfoSerializer : KSerializer<ImageInfo> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ImageInfo") {
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
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): ImageInfo = decoder.decodeStructure(descriptor) {
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
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        require(path != null)
        require(width != null)
        require(height != null)

        return@decodeStructure ImageInfo(
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
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: ImageInfo) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, MediaType.Image.toString())
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
    }
}