package info

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
        element<Int>("id")
        element<String>("path")
        element<Int>("width")
        element<Int>("height")
        element<String>("name")
        element<String>("description")
        element<Boolean>("favorite")
        element<List<String>>("tags")
        element<String?>("source")
        element<String?>("rating")
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): ImageInfo = decoder.decodeStructure(descriptor) {
        var id: Int? = null
        var path: String? = null
        var width: Int? = null
        var height: Int? = null
        var name: String? = null
        var description: String? = null
        var favorite: Boolean? = null
        var tags: List<String>? = null
        var source: String? = null
        var rating: String? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> id = decodeIntElement(descriptor, index)
                1 -> path = decodeStringElement(descriptor, index)
                2 -> width = decodeIntElement(descriptor, index)
                3 -> height = decodeIntElement(descriptor, index)
                4 -> name = decodeStringElement(descriptor, index)
                5 -> description = decodeStringElement(descriptor, index)
                6 -> favorite = decodeBooleanElement(descriptor, index)
                7 -> tags = decodeSerializableElement(descriptor, index, ListSerializer(String.serializer()))
                8 -> source = decodeNullableSerializableElement(descriptor, index, String.serializer())
                9 -> rating = decodeNullableSerializableElement(descriptor, index, String.serializer())
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        require(
            path != null
                    && width != null
                    && height != null
                    && name != null
                    && description != null
                    && favorite != null
                    && tags != null
        )

        return@decodeStructure ImageInfo(
            id = id ?: -1,
            path = path,
            width = width,
            height = height,
            name = name,
            description = description,
            favorite = favorite,
            tags = tags.toMutableList(),
            source = source,
            rating = rating,
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: ImageInfo) = encoder.encodeStructure(descriptor) {
        encodeIntElement(descriptor, 0, value.id)
        encodeStringElement(descriptor, 1, value.path)
        encodeIntElement(descriptor, 2, value.width)
        encodeIntElement(descriptor, 3, value.height)
        encodeStringElement(descriptor, 4, value.name)
        encodeStringElement(descriptor, 5, value.description)
        encodeBooleanElement(descriptor, 6, value.favorite)
        encodeSerializableElement(descriptor, 7, ListSerializer(String.serializer()), value.tags)
        encodeNullableSerializableElement(descriptor, 8, String.serializer(), value.source)
        encodeNullableSerializableElement(descriptor, 9, String.serializer(), value.rating)
    }
}