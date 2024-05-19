package properties

import properties.data.Data
import properties.data.DataContainer
import properties.data.backup.BackupManager
import properties.settings.Settings
import properties.settings.SettingsContainer

object Properties {
    const val VERSION = "0.4.3"
    const val DATA_VERSION = "3.4"

    private val languageContainer: LanguageContainer = LanguageContainer()
    fun language(): Language = languageContainer.language
    fun loadLanguage(lang: String = "en") { languageContainer.load(lang) }

    private val settingsContainer: SettingsContainer = SettingsContainer()
    fun settings(): Settings = settingsContainer.settings
    fun loadSettings() { settingsContainer.load() }
    fun saveSettings() { settingsContainer.save() }

    private val styleContainer: StyleContainer = StyleContainer()
    fun style(): Style = styleContainer.style
    fun loadStyle() { styleContainer.load() }
    fun saveStyle() { styleContainer.save() }

    private val dataContainer: DataContainer = DataContainer()
    fun imagesData(): Data = dataContainer.data
    fun loadData() { dataContainer.load() }
    fun saveData() { dataContainer.save() }

    val backup = BackupManager()
}
