package properties.settings

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import properties.DataFolder

class SettingsContainer {
    lateinit var settings: Settings
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    fun load() {
        settings = if (DataFolder.settingsFile.exists()) {
            try {
                json.decodeFromString(DataFolder.settingsFile.readText())

            } catch (e: Exception) { Settings() }
        } else { Settings() }
    }

    fun save() {
        DataFolder.settingsFile.writeText(json.encodeToString(settings))
    }
}
