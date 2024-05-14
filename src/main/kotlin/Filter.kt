import info.ImageGroup
import info.ImageInfo

class Filter {
    private var filterTags: List<String>? = null
    private var filterAntiTags: List<String>? = null
    private var imageGroup: ImageGroup? = null
    private var filterFavorite: Boolean = false

    fun tags(tagStorage: TagStorage): Filter {
        this.filterTags = tagStorage.selectedTags
        this.filterAntiTags = tagStorage.selectedAntiTags
        return this
    }

    fun group(imageGroup: ImageGroup): Filter {
        this.imageGroup = imageGroup
        return this
    }

    fun favorite(): Filter {
        filterFavorite = true
        return this
    }

    fun filter(images: Collection<ImageInfo>): Collection<ImageInfo> = images
        .filter { imageInfo ->
            val byGroup = imageGroup?.imagePaths?.contains(imageInfo.path) ?: true
            val byAntiTags = filterAntiTags?.none { imageInfo.tags.contains(it) } ?: true
            val byTags = filterTags?.all { imageInfo.tags.contains(it) } ?: true
            val byFavorite = (!filterFavorite || imageInfo.favorite)

            byGroup && byAntiTags && byTags && byFavorite
        }

    fun filterGroups(imageGroups: Collection<ImageGroup>): Collection<ImageGroup> = imageGroups
        .filter { imageGroup ->
            val byGroup = imageGroup.imagePaths.containsAll(imageGroup.imagePaths)
            val byAntiTags = filterAntiTags?.none { imageGroup.tags.contains(it) } ?: true
            val byTags = filterTags?.all { imageGroup.tags.contains(it) } ?: true
            val byFavorite = (!filterFavorite || imageGroup.favorite)

            byGroup && byAntiTags && byTags && byFavorite
        }
}