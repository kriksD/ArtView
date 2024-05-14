package properties.data

import info.ImageGroup
import info.ImageInfo
import TagCategory
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
import uniqueId

class DataSerializer : KSerializer<Data> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Data") {
        element<DataMeta>("meta")
        element<MutableList<TagCategory>>("tags")
        element<MutableList<ImageInfo>>("images")
        element<MutableList<ImageGroup>>("imageGroups")
    }

    override fun deserialize(decoder: Decoder): Data = decoder.decodeStructure(descriptor) {
        var meta: DataMeta? = null
        var tags: List<TagCategory>? = null
        var images: List<ImageInfo>? = null
        var imageGroups: List<ImageGroup>? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> meta = decodeSerializableElement(descriptor, 0, DataMeta.serializer())
                1 -> tags = decodeSerializableElement(descriptor, 1, ListSerializer(TagCategory.serializer()))
                2 -> images = decodeSerializableElement(descriptor, 2, ListSerializer(ImageInfo.serializer()))
                3 -> imageGroups = decodeSerializableElement(descriptor, 3, ListSerializer(ImageGroup.serializer()))
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        images = checkMissingIDsInImageInfo(images ?: listOf())
        imageGroups = checkMissingIDsInImageGroup(imageGroups ?: listOf())

        return@decodeStructure Data(
            meta = meta ?: DataMeta(),
            tags = tags ?: listOf(),
            images = images,
            imageGroups = imageGroups,
        )
    }

    private fun checkMissingIDsInImageInfo(list: List<ImageInfo>): List<ImageInfo> {
        if (list.none { it.id == -1 }) return list

        val newList = list.toMutableList()
        newList.forEachIndexed { index, hasID ->
            if (hasID.id == -1) {
                newList[index] = hasID.copy(id = newList.uniqueId())
            }
        }

        return newList
    }

    private fun checkMissingIDsInImageGroup(list: List<ImageGroup>): List<ImageGroup> {
        if (list.none { it.id == -1 }) return list

        val newList = list.toMutableList()
        newList.forEachIndexed { index, hasID ->
            if (hasID.id == -1) {
                newList[index] = hasID.copy(id = newList.uniqueId())
            }
        }

        return newList
    }

    override fun serialize(encoder: Encoder, value: Data) = encoder.encodeStructure(descriptor) {
        encodeSerializableElement(descriptor, 0, DataMeta.serializer(), value.meta)
        encodeSerializableElement(descriptor, 1, ListSerializer(TagCategory.serializer()), value.tags)
        encodeSerializableElement(descriptor, 2, ListSerializer(ImageInfo.serializer()), value.images)
        encodeSerializableElement(descriptor, 3, ListSerializer(ImageGroup.serializer()), value.imageGroups)
    }
}