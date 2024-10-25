package tag

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
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

class TagCategorySerializer : KSerializer<TagCategory> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TagCategory") {
        element<String>("name")
        element<List<String>>("tags")
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): TagCategory = decoder.decodeStructure(descriptor) {
        var name: String? = null
        var tags: List<String>? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> name = decodeStringElement(descriptor, index)
                1 -> tags = decodeSerializableElement(descriptor, index, ListSerializer(String.serializer()))
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure TagCategory(
            name = name ?: "",
            tags = tags?.toMutableStateList() ?: mutableStateListOf(),
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: TagCategory) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, value.name)
        encodeSerializableElement(descriptor, 1, ListSerializer(String.serializer()), value.tags.toList())
    }
}