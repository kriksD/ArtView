import info.ImageInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import properties.Properties

class ImageLoader {
    private val loadRequests = mutableListOf<ImageInfo>()
    private val unloadRequests = mutableListOf<ImageInfo>()

    val isLoading get() = loadRequests.isNotEmpty() || unloadRequests.isNotEmpty()
    val requestAmount get() = loadRequests.size + unloadRequests.size

    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var isRunning = true

    fun loadNext(image: ImageInfo) {
        scope.launch {
            mutex.withLock {
                if (loadRequests.contains(image)) return@withLock
                if (unloadRequests.contains(image)) unloadRequests.remove(image)

                loadRequests.add(image)
            }
        }
    }

    fun unloadNext(image: ImageInfo) {
        scope.launch {
            mutex.withLock {
                if (unloadRequests.contains(image)) return@withLock
                if (loadRequests.contains(image)) loadRequests.remove(image)

                unloadRequests.add(image)
            }
        }
    }

    fun cancel() {
        scope.launch {
            mutex.withLock {
                isRunning = false
                Properties.imagesData().images.filter { it.isLoaded }.forEach { it.unload() }
                loadRequests.clear()
                unloadRequests.clear()
            }
        }
    }

    suspend fun load() = coroutineScope {
        launch(Dispatchers.Default) {
            while(isRunning) {
                if (unloadRequests.isNotEmpty()) {
                    val image = mutex.withLock {
                        unloadRequests.removeFirst()
                    }
                    image.unload()

                } else if (loadRequests.isNotEmpty()) {
                    val image = mutex.withLock {
                        loadRequests.removeFirst()
                    }
                    image.load()

                } else {
                    delay(20)
                }
            }
        }
    }
}