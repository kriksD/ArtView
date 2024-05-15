package booruClient.clients

import booruClient.BooruPost
import booruClient.parser.DanbooruPostJSONParser
import booruClient.parser.GelbooruPostJSONParser
import booruClient.parser.Rule34PostJSONParser
import booruClient.parser.SafebooruPostJSONParser
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.delay
import kotlin.math.max

object BooruClient {
    private val client = HttpClient(OkHttp) {
        install(HttpTimeout)
    }

    private val boorus = mapOf(
        "Danbooru" to Danbooru(client),
        "Gelbooru" to Gelbooru(client),
        "Rule34" to Rule34(client),
        "Yandere" to Yandere(client),
    )

    suspend fun loadPost(link: String): BooruPost? {
        boorus.forEach { (_, booru) ->
            if (link.startsWith(booru.basePostURL)) {
                return booru.loadPost(link)
            }
        }

        return tryEverything(link)
    }

    private suspend fun tryEverything(link: String): BooruPost? {
        val parsers = listOf(
            DanbooruPostJSONParser(),
            GelbooruPostJSONParser(),
            Rule34PostJSONParser(),
            SafebooruPostJSONParser(),
        )

        parsers.forEach { parser ->
            val timeStart = System.currentTimeMillis()

            boorus.forEach { (name, booru) ->
                println(name)
                val post = booru.loadPost(link, parser, true)
                if (post != null) return post
            }

            delay(max(100, 1100 - (System.currentTimeMillis() - timeStart)))
        }

        return null
    }
}