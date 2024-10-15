package info.group

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

class MediaGroupSerializer : KSerializer<MediaGroup> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ImageGroup") {
        element<Int>("id")
        element<List<Int>>("media_ids")
        element<String>("name")
        element<String>("description")
        element<Boolean>("favorite")
        element<List<String>>("tags")
    }

    override fun deserialize(decoder: Decoder): MediaGroup = decoder.decodeStructure(descriptor) {
        var id: Int? = null
        var mediaIDs: List<Int>? = null
        var name: String? = null
        var description: String? = null
        var favorite: Boolean? = null
        var tags: List<String>? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> id = decodeIntElement(descriptor, index)
                1 -> mediaIDs = decodeSerializableElement(descriptor, index, ListSerializer(Int.serializer()))
                2 -> name = decodeStringElement(descriptor, index)
                3 -> description = decodeStringElement(descriptor, index)
                4 -> favorite = decodeBooleanElement(descriptor, index)
                5 -> tags = decodeSerializableElement(descriptor, index, ListSerializer(String.serializer()))
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        require(
            mediaIDs != null
        )

        return@decodeStructure MediaGroup(
            id = id ?: -1,
            mediaIDs = mediaIDs.toMutableList(),
            name = name ?: "",
            description = description ?: "",
            favorite = favorite ?: false,
            tags = tags?.toMutableList() ?: mutableListOf(),
        )
    }

    override fun serialize(encoder: Encoder, value: MediaGroup) = encoder.encodeStructure(descriptor) {
        encodeIntElement(descriptor, 0, value.id)
        encodeSerializableElement(descriptor, 1, ListSerializer(Int.serializer()), value.mediaIDs)
        encodeStringElement(descriptor, 2, value.name)
        encodeStringElement(descriptor, 3, value.description)
        encodeBooleanElement(descriptor, 4, value.favorite)
        encodeSerializableElement(descriptor, 5, ListSerializer(String.serializer()), value.tags)
    }
}