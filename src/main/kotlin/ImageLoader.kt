import androidx.compose.runtime.mutableStateListOf
import properties.Properties

class ImageLoader {
    private val filtered = mutableListOf<ImageInfo>()
    val loadedList = mutableStateListOf<ImageInfo>()
    private var index = 0

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
    }

    fun reset() {
        loadedList.clear()
        index = 0
    }
}