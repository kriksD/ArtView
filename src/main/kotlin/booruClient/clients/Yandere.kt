package booruClient.clients

import booruClient.BooruPost
import booruClient.parser.BooruPostJSONParser
import booruClient.parser.Rule34PostJSONParser
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class Yandere(override val client: HttpClient) : Booru() {

    override val baseURL: String get() = "https://yande.re"
    override val basePostURL: String get() = "https://yande.re/post/show/"

    override val defaultParser: BooruPostJSONParser = Rule34PostJSONParser()

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

    override fun createLink(link: String): String {
        val root = link.substringBefore("/post/show/")
        val id = link.substringAfter("/post/show/").substringBefore("/").substringBefore("&")
        return "$root/post.json?tags=id:$id"
    }
}