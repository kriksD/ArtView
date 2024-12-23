package properties

import kotlinx.serialization.json.*
import java.io.File

class LanguageContainer {
    lateinit var language: Language
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun load(lang: String = "en") {
        val file = File("data/language/${lang}.json")
        if (!file.exists()) return

        language = json.decodeFromString(file.readText())
    }
}
