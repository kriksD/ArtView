package danbooruClient

import androidx.compose.ui.graphics.ImageBitmap
import getImageBitmap
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import java.util.*

class ImageLoaderClient {
    private val client = HttpClient(OkHttp) {
        install(HttpTimeout)
    }

    suspend fun loadImageBitmap(link: String): ImageBitmap? {
        return try {
            val result = client.get(link) {
                timeout {
                    requestTimeoutMillis = 20_000
                    socketTimeoutMillis = 20_000
                }
            }

            if (result.status.value !in 200..299) return null

            val imageBytes = result.bodyAsChannel().toByteArray()
            val base64String = Base64.getEncoder().encodeToString(imageBytes)
            val decodedImageBytes = Base64.getDecoder().decode(base64String)

            getImageBitmap(decodedImageBytes)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}