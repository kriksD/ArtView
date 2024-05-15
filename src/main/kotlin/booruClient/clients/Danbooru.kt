package booruClient.clients

import booruClient.BooruPost
import booruClient.parser.BooruPostJSONParser
import booruClient.parser.DanbooruPostJSONParser
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class Danbooru(override val client: HttpClient) : Booru() {

    override val baseURL: String get() = "https://danbooru.donmai.us"
    override val basePostURL: String get() = "https://danbooru.donmai.us/posts/"

    override val defaultParser: BooruPostJSONParser = DanbooruPostJSONParser()

    override suspend fun loadPost(
        link: String,
        parser: BooruPostJSONParser,
        noPrintStackTrace: Boolean,
    ): BooruPost? {
        return try {
            val result = client.get(createLink(link)) {
                timeout {
                    requestTimeoutMillis = 20_000
                    socketTimeoutMillis = 20_000
                }
            }

            parser.parseJson(result.bodyAsText(), link)

        } catch (e: Exception) {
            if (!noPrintStackTrace) e.printStackTrace()
            null
        }
    }

    override fun createLink(link: String) = if (link.endsWith(".json")) link else "$link.json"
}