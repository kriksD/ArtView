package properties.settings

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

class SettingsSerializer : KSerializer<Settings> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Settings") {
        element<String>("language")
        element<String>("background")
        element<MutableList<String>>("selected_tags_by_default")
        element<MutableList<String>>("anti_selected_tags_by_default")
        element<Boolean>("auto_select_created_tags")
        element<Boolean>("add_tags_to_created_groups")
        element<TagControlsPosition>("add_tag_button_position")
        element<TagControlsPosition>("search_tag_button_position")
        element<Boolean>("show_debug")

        element<String>("booru_tags_category_name")
        element<String>("character_tags_category_name")
        element<String>("copyright_tags_category_name")
        element<String>("artist_tags_category_name")
        element<String>("meta_tags_category_name")

        element<Int>("backup_limit")
        element<Int>("loading_threads")
    }

    private val listSerializer = ListSerializer(String.serializer())

    override fun deserialize(decoder: Decoder): Settings = decoder.decodeStructure(descriptor) {
        val settings = Settings()

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                -1 -> break
                0 -> settings.language = decodeStringElement(descriptor, 0)
                1 -> settings.background = decodeStringElement(descriptor, 1)
                2 -> settings.selectedTagsByDefault.also { it.clear() }.addAll(decodeSerializableElement(descriptor, 2, listSerializer))
                3 -> settings.antiSelectedTagsByDefault.also { it.clear() }.addAll(decodeSerializableElement(descriptor, 3, listSerializer))
                4 -> settings.autoSelectCreatedTags = decodeBooleanElement(descriptor, 4)
                5 -> settings.addTagsToCreatedGroups = decodeBooleanElement(descriptor, 5)
                6 -> settings.addTagButtonPosition = decodeSerializableElement(descriptor, 6, TagControlsPosition.serializer())
                7 -> settings.filterTagButtonPosition = decodeSerializableElement(descriptor, 7, TagControlsPosition.serializer())
                8 -> settings.showDebug = decodeBooleanElement(descriptor, 8)

                9 -> settings.booruTagsCategoryName = decodeStringElement(descriptor, 9)
                10 -> settings.characterTagsCategoryName = decodeStringElement(descriptor, 10)
                11 -> settings.copyrightTagsCategoryName = decodeStringElement(descriptor, 11)
                12 -> settings.artistTagsCategoryName = decodeStringElement(descriptor, 12)
                13 -> settings.metaTagsCategoryName = decodeStringElement(descriptor, 13)

                14 -> settings.backupLimit = decodeIntElement(descriptor, 14)
                15 -> settings.loadingThreads = decodeIntElement(descriptor, 15)

                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return@decodeStructure settings
    }

    override fun serialize(encoder: Encoder, value: Settings) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, 0, value.language)
        encodeStringElement(descriptor, 1, value.background)
        encodeSerializableElement(descriptor, 2, listSerializer, value.selectedTagsByDefault)
        encodeSerializableElement(descriptor, 3, listSerializer, value.antiSelectedTagsByDefault)
        encodeBooleanElement(descriptor, 4, value.autoSelectCreatedTags)
        encodeBooleanElement(descriptor, 5, value.addTagsToCreatedGroups)
        encodeSerializableElement(descriptor, 6, TagControlsPosition.serializer(), value.addTagButtonPosition)
        encodeSerializableElement(descriptor, 7, TagControlsPosition.serializer(), value.filterTagButtonPosition)
        encodeBooleanElement(descriptor, 8, value.showDebug)

        encodeStringElement(descriptor, 9, value.booruTagsCategoryName)
        encodeStringElement(descriptor, 10, value.characterTagsCategoryName)
        encodeStringElement(descriptor, 11, value.copyrightTagsCategoryName)
        encodeStringElement(descriptor, 12, value.artistTagsCategoryName)
        encodeStringElement(descriptor, 13, value.metaTagsCategoryName)

        encodeIntElement(descriptor, 14, value.backupLimit)
        encodeIntElement(descriptor, 15, value.loadingThreads)
    }
}