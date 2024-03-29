class FilterBuilder {
    private var filterTags: List<String>? = null
    private var filterAntiTags: List<String>? = null
    private var imageGroup: ImageGroup? = null
    private var filterFavorite: Boolean = false

    fun tags(filterTags: List<String>): FilterBuilder {
        this.filterTags = filterTags
        return this
    }

    fun antiTags(filterAntiTags: List<String>): FilterBuilder {
        this.filterAntiTags = filterAntiTags
        return this
    }

    fun group(imageGroup: ImageGroup): FilterBuilder {
        this.imageGroup = imageGroup
        return this
    }

    fun favorite(): FilterBuilder {
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