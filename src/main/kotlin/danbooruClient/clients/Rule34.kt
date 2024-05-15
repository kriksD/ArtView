package danbooruClient.clients

import danbooruClient.BooruPost
import danbooruClient.parser.BooruPostJSONParser
import danbooruClient.parser.Rule34PostJSONParser
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class Rule34(override val client: HttpClient) : Booru() {

    override val baseURL: String get() = "https://rule34.xxx"
    override val basePostURL: String get() = "https://rule34.xxx/index.php?page=post&s=view&id="

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
        val root = link.substringBefore("/index.php?").substringAfter("https://")
        val id = link.substringAfter("id=").substringBefore("/").substringBefore("&")
        return "https://api.$root/index.php?page=dapi&s=post&q=index&id=$id&json=1"
    }
}