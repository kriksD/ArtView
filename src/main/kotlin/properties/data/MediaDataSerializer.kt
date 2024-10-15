package properties.data

import info.group.MediaGroup
import info.media.ImageInfo
import info.media.MediaInfo
import info.media.serializers.MediaInfoSerializer
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
import utilities.uniqueId

class MediaDataSerializer : KSerializer<MediaData> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("MediaData") {
        element<String>("data_version")
        element<MutableList<ImageInfo>>("media")
        element<MutableList<MediaGroup>>("imageGroups")
    }

    override fun deserialize(decoder: Decoder): MediaData = decoder.decodeStructure(descriptor) {
        var dataVersion: String? = null
        var mediaList: List<MediaInfo>? = null
        var mediaGroups: List<MediaGroup>? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> dataVersion = decodeStringElement(descriptor, 0)
                1 -> mediaList = decodeSerializableElement(descriptor, 2, ListSerializer(MediaInfoSerializer))
                2 -> mediaGroups = decodeSerializableElement(descriptor, 3, ListSerializer(MediaGroup.serializer()))
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        mediaList = checkMissingIDsInImageInfo(mediaList ?: listOf())
        mediaGroups = checkMissingIDsInImageGroup(mediaGroups ?: listOf())

        return@decodeStructure MediaData(
            dataVersion = dataVersion ?: Properties.DATA_VERSION,
            mediaList = mediaList,
            mediaGroups = mediaGroups,
        )
    }

    private fun checkMissingIDsInImageInfo(list: List<MediaInfo>): List<MediaInfo> {
        if (list.none { it.id == -1 }) return list

        val newList = list.toMutableList()
        newList.forEachIndexed { index, hasID ->
            if (hasID.id == -1) {
                newList[index] = hasID.copy(id = newList.uniqueId())
            }
        }

        return newList
    }

    private fun checkMissingIDsInImageGroup(list: List<MediaGroup>): List<MediaGroup> {
        if (list.none { it.id == -1 }) return list

        val newList = list.toMutableList()
        newList.forEachIndexed { index, hasID ->
            if (hasID.id == -1) {
                newList[index] = hasID.copy(id = newList.uniqueId())
            }
        }

        return newList
    }

    override fun serialize(encoder: Encoder, value: MediaData) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, value.dataVersion)
        encodeSerializableElement(descriptor, 1, ListSerializer(MediaInfoSerializer), value.mediaList)
        encodeSerializableElement(descriptor, 2, ListSerializer(MediaGroup.serializer()), value.mediaGroups)
    }
}