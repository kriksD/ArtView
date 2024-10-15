package loader

import info.media.MediaInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mediaData
import settings

class ThumbnailLoader {
    private val loadRequests = MutableList(settings.loadingThreads) { mutableListOf<MediaInfo>() }
    private val unloadRequests = MutableList(settings.loadingThreads) { mutableListOf<MediaInfo>() }

    val isLoading get() = loadRequests.flatten().isNotEmpty() || unloadRequests.flatten().isNotEmpty()
    val requestAmount get() = loadRequests.sumOf { it.size } + unloadRequests.sumOf { it.size }
    val threadCount get() = loadRequests.size

    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var isRunning = true

    fun loadNext(info: MediaInfo) {
        scope.launch {
            mutex.withLock {
                val threadIndex = leastBusyThread()

                if (loadRequests[threadIndex].contains(info)) return@withLock
                if (unloadRequests[threadIndex].contains(info)) unloadRequests[threadIndex].remove(info)
                if (info.isThumbnailLoaded) return@withLock

                loadRequests[threadIndex].add(info)
            }
        }
    }

    fun unloadNext(info: MediaInfo) {
        scope.launch {
            mutex.withLock {
                val threadIndex = leastBusyThread()

                if (unloadRequests[threadIndex].contains(info)) return@withLock
                if (loadRequests[threadIndex].contains(info)) loadRequests[threadIndex].remove(info)
                if (!info.isThumbnailLoaded) return@withLock

                unloadRequests[threadIndex].add(info)
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
        mediaData.mediaList.filter { it.isThumbnailLoaded }.forEach { it.unloadThumbnail() }
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
                        image?.unloadThumbnail()

                    } else if (loadRequests[threadIndex].isNotEmpty()) {
                        val image = mutex.withLock {
                            loadRequests[threadIndex].removeFirstOrNull()
                        }
                        image?.loadThumbnail()

                    } else {
                        delay(20)
                    }
                }
            }
        }
    }
}