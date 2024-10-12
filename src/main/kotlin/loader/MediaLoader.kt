package loader

import info.MediaInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import properties.Properties
import settings

class MediaLoader {
    private val loadRequests = MutableList(settings.loadingThreads) { mutableListOf<MediaInfo>() }
    private val unloadRequests = MutableList(settings.loadingThreads) { mutableListOf<MediaInfo>() }

    val isLoading get() = loadRequests.flatten().isNotEmpty() || unloadRequests.flatten().isNotEmpty()
    val requestAmount get() = loadRequests.sumOf { it.size } + unloadRequests.sumOf { it.size }
    val threadCount get() = loadRequests.size

    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var isRunning = true

    fun loadNext(image: MediaInfo) {
        scope.launch {
            mutex.withLock {
                val threadIndex = leastBusyThread()

                if (loadRequests[threadIndex].contains(image)) return@withLock
                if (unloadRequests[threadIndex].contains(image)) unloadRequests[threadIndex].remove(image)
                if (image.isLoaded) return@withLock

                loadRequests[threadIndex].add(image)
            }
        }
    }

    fun unloadNext(image: MediaInfo) {
        scope.launch {
            mutex.withLock {
                val threadIndex = leastBusyThread()

                if (unloadRequests[threadIndex].contains(image)) return@withLock
                if (loadRequests[threadIndex].contains(image)) loadRequests[threadIndex].remove(image)
                if (!image.isLoaded) return@withLock

                unloadRequests[threadIndex].add(image)
            }
        }
    }

    private fun leastBusyThread(): Int {
        var minSize = -1
        var minSizeIndex = -1

        repeat(settings.loadingThreads) { threadIndex ->
            val size = loadRequests[threadIndex].size + unloadRequests[threadIndex].size

            if (minSize == -1 || size < minSize) {
                minSize = size
                minSizeIndex = threadIndex
            }
        }

        return minSizeIndex
    }

    fun cancel() {
        scope.launch {
            mutex.withLock {
                isRunning = false
                resetRequests()
            }
        }
    }

    fun reset() {
        scope.launch {
            mutex.withLock {
                resetRequests()
            }
        }
    }

    private fun resetRequests() {
        Properties.mediaData().mediaList.filter { it.isLoaded }.forEach { it.unload() }
        loadRequests.forEach { it.clear() }
        unloadRequests.forEach { it.clear() }
    }

    suspend fun load() = coroutineScope {
        repeat(settings.loadingThreads) { threadIndex ->
            launch(Dispatchers.Default) {
                while(isRunning) {
                    if (unloadRequests[threadIndex].isNotEmpty()) {
                        val image = mutex.withLock {
                            unloadRequests[threadIndex].removeFirstOrNull()
                        }
                        image?.unload()

                    } else if (loadRequests[threadIndex].isNotEmpty()) {
                        val image = mutex.withLock {
                            loadRequests[threadIndex].removeFirstOrNull()
                        }
                        image?.load()

                    } else {
                        delay(20)
                    }
                }
            }
        }
    }
}