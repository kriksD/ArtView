package booruClient.clients

import booruClient.BooruPost
import booruClient.parser.BooruPostJSONParser
import io.ktor.client.*

abstract class Booru {
    protected abstract val client: HttpClient
    abstract val baseURL: String
    abstract val basePostURL: String

    protected abstract val defaultParser: BooruPostJSONParser

    abstract suspend fun loadPost(
        link: String,
        parser: BooruPostJSONParser = defaultParser,
        noPrintStackTrace: Boolean = false,
    ): BooruPost?

    protected abstract fun createLink(link: String): String
}