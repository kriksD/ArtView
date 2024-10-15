package properties.data

import tag.TagCategory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import properties.Properties

class TagDataSerializer : KSerializer<TagData> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TagData") {
        element<String>("data_version")
        element<List<TagCategory>>("tags")
    }

    override fun deserialize(decoder: Decoder): TagData = decoder.decodeStructure(descriptor) {
        var dataVersion: String? = null
        var tags: List<TagCategory> = mutableListOf()

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> dataVersion = decodeStringElement(descriptor, 0)
                1 -> tags = decodeSerializableElement(descriptor, 1, ListSerializer(TagCategory.serializer()))
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure TagData(
            dataVersion = dataVersion ?: Properties.DATA_VERSION,
            tags = tags,
        )
    }

    override fun serialize(encoder: Encoder, value: TagData) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, value.dataVersion)
        encodeSerializableElement(descriptor, 1, ListSerializer(TagCategory.serializer()), value.tags)
    }
}