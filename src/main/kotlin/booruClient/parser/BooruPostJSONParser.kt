package booruClient.parser

import booruClient.BooruPost

interface BooruPostJSONParser {
    suspend fun parseJson(json: String, link: String): BooruPost?
}