import androidx.compose.runtime.mutableStateListOf
import properties.Properties

class ImageLoader {
    private val filtered = mutableListOf<ImageInfo>()
    val loadedList = mutableStateListOf<ImageInfo>()
    private var index = 0

    private var lastFilter: FilterBuilder = FilterBuilder()

    fun loadNext(): ImageInfo? {
        if (index > filtered.lastIndex) return null

        return filtered[index].also {
            loadedList.add(it)
            index++
        }
    }

    fun filter(filter: FilterBuilder) {
        filtered.clear()
        filtered.addAll(
            filter.filter(Properties.imagesData().images)
        )
        lastFilter = filter
    }

    fun reset() {
        loadedList.clear()
        index = 0
    }

    fun update() {
        val lastLoadedAmount = loadedList.size

        reset()
        filter(lastFilter)

        repeat(lastLoadedAmount + 1) {
            loadNext()
        }
    }
}