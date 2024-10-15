package properties

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StyleContainer {
    lateinit var style: Style

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun load() {
        style = try {
            if (DataFolder.styleFile.exists()) {
                json.decodeFromString(DataFolder.styleFile.readText())
            } else {
                Style()
            }
        } catch (e: Exception) {
            Style()
        }
    }

    fun save() {
        DataFolder.styleFile.writeText(json.encodeToString(style))
    }
}