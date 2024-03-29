package properties.data

import ImageInfo
import TagCategory
import getImageBitmap
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import properties.data.oldData.Data2d0
import properties.data.oldData.Data1d0
import java.io.File

class DataConverter(private val file: File) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    fun loadAndConvert(): Data? {
        if (!file.exists()) return null

        val fileContent = file.readText()
        val version = Json.parseToJsonElement(fileContent)
            .jsonObject["meta"]
            ?.jsonObject
            ?.get("version")
            ?.jsonPrimitive
            ?.content

        return when (version) {
            "3.0" -> Json.decodeFromString(fileContent)
            null -> tryToLoadData() ?: tryToLoadOldData()
            else -> null
        }
    }

    /**
     * Tries to load 2.0 data and convert to the newest data version
     */
    private fun tryToLoadData(): Data? {
        return try {
            if (file.exists()) {
                val oldData = json.decodeFromString<Data2d0>(file.readText())
                Data(
                    DataMeta(),
                    oldData.tags,
                    oldData.images.mapNotNull {
                        val image = getImageBitmap(it.path) ?: return@mapNotNull null

                        ImageInfo(
                            it.path,
                            image.width,
                            image.height,
                            it.name,
                            it.description,
                            it.favorite,
                            it.tags,
                        )
                    }.toMutableList(),
                )

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