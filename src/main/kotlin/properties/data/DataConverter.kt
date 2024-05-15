package properties.data

import info.ImageInfo
import TagCategory
import getImageBitmap
import getImageDimensions
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import properties.Properties
import properties.data.oldData.Data2d0
import properties.data.oldData.Data1d0
import uniqueId
import java.awt.Dimension
import java.io.File

class DataConverter(private val file: File) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    private val version3 = listOf("3.0", "3.1", "3.2", "3.3", "3.4")

    fun loadAndConvert(): Data? {
        if (!file.exists()) return null

        val fileContent = file.readText()
        val version = Json.parseToJsonElement(fileContent)
            .jsonObject["meta"]
            ?.jsonObject
            ?.get("version")
            ?.jsonPrimitive
            ?.content

        return if (version3.contains(version)) {
            Json.decodeFromString(fileContent)

        } else if (version == null) {
            tryToLoadData() ?: tryToLoadOldData()

        } else {
            null
        }
    }

    /**
     * Tries to load 2.0 data and convert to the newest data version
     */
    private fun tryToLoadData(): Data? {
        return try {
            if (file.exists()) {
                val oldData = json.decodeFromString<Data2d0>(file.readText())
                val images = oldData.images.mapNotNull {
                    val dimensions = getImageDimensions(it.path)
                        ?: run {
                            val image = getImageBitmap(it.path) ?: return@run null
                            Dimension(image.width, image.height)
                        }
                        ?: run { println("Failed to convert: ${it.path}"); return@mapNotNull null }

                    ImageInfo(
                        Properties.imagesData().images.uniqueId(),
                        it.path,
                        dimensions.width,
                        dimensions.height,
                        it.name,
                        it.description,
                        it.favorite,
                        it.tags,
                    )
                }.toMutableList()

                Data(DataMeta(), oldData.tags, images, oldData.imageGroups)

            } else { null }
        } catch (e: Exception) { null }
    }

    /**
     * Tries to load 1.0 data and convert to the newest data version
     */
    private fun tryToLoadOldData(): Data? {
        return try {
            if (file.exists()) {
                val oldData = json.decodeFromString<Data1d0>(file.readText())
                Data(
                    DataMeta(),
                    mutableListOf(TagCategory("Other", oldData.tags)),
                    oldData.images,
                )

            } else { null }
        } catch (e: Exception) { null }
    }
}