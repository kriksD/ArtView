import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import properties.Properties

enum class LoadingState {
    Load, Unload;
}

data class LoadRequest(
    val image: ImageInfo,
    val state: LoadingState,
)

class ImageLoader {
    val filteredImages = mutableStateListOf<ImageInfo>()
    private var lastFilter: FilterBuilder = FilterBuilder()

    private val loadingChannel = Channel<LoadRequest>(Channel.UNLIMITED)

    fun filter(filter: FilterBuilder) {
        filteredImages.clear()
        filteredImages.addAll(
            filter.filter(Properties.imagesData().images)
        )
        lastFilter = filter
    }

    fun update() {
        filteredImages.clear()
        filteredImages.addAll(lastFilter.filter(Properties.imagesData().images))
    }

    fun reset() {
        filteredImages.clear()
        filteredImages.addAll(lastFilter.filter(Properties.imagesData().images))
    }

    suspend fun loadNext(image: ImageInfo) {
        if (image.isLoaded) return

        try {
            loadingChannel.send(LoadRequest(image, LoadingState.Load))
        } catch (e: Exception) {
            // Handle the case where sending to the channel fails (e.g., channel is closed)
        }
    }

    suspend fun unloadNext(image: ImageInfo) {
        if (!image.isLoaded) return

        try {
            loadingChannel.send(LoadRequest(image, LoadingState.Unload))
        } catch (e: Exception) {
            // Handle the case where sending to the channel fails (e.g., channel is closed)
        }
    }

    suspend fun load() = coroutineScope {
        launch(Dispatchers.IO) {
            for (loadRequest in loadingChannel) {
                try {
                    when (loadRequest.state) {
                        LoadingState.Load -> {
                            if (loadRequest.image.isLoaded) continue
                            loadRequest.image.load()
                            println(1)
                        }
                        LoadingState.Unload -> {
                            if (!loadRequest.image.isLoaded) continue
                            loadRequest.image.unload()
                        }
                    }
                } catch (e: Exception) {
                    // Handle the case where sending to the channel fails (e.g., channel is closed)
                }
            }
        }
    }
}