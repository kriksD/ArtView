package danbooruClient.parser

import danbooruClient.BooruPost

interface BooruPostJSONParser {
    suspend fun parseJson(json: String, link: String): BooruPost?
}