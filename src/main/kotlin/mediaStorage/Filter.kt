package mediaStorage

import info.group.MediaGroup
import info.media.MediaInfo
import tag.TagStorage

class Filter {
    private var filterTags: List<String>? = null
    private var filterAntiTags: List<String>? = null
    private var mediaGroup: MediaGroup? = null
    private var filterFavorite: Boolean = false

    fun tags(tagStorage: TagStorage): Filter {
        this.filterTags = tagStorage.selectedTags
        this.filterAntiTags = tagStorage.selectedAntiTags
        return this
    }

    fun group(mediaGroup: MediaGroup): Filter {
        this.mediaGroup = mediaGroup
        return this
    }

    fun favorite(): Filter {
        filterFavorite = true
        return this
    }

    fun filter(mediaList: Collection<MediaInfo>): Collection<MediaInfo> = mediaList
        .filter { mediaInfo ->
            val byGroup = mediaGroup?.mediaIDs?.contains(mediaInfo.id) ?: true
            val byAntiTags = filterAntiTags?.none { mediaInfo.tags.contains(it) } ?: true
            val byTags = filterTags?.all { mediaInfo.tags.contains(it) } ?: true
            val byFavorite = (!filterFavorite || mediaInfo.favorite)

            byGroup && byAntiTags && byTags && byFavorite
        }

    fun filterGroups(mediaGroups: Collection<MediaGroup>): Collection<MediaGroup> = mediaGroups
        .filter { mGroup ->
            val byGroup = mGroup.mediaIDs.containsAll(mGroup.mediaIDs)
            val byAntiTags = filterAntiTags?.none { mGroup.tags.contains(it) } ?: true
            val byTags = filterTags?.all { mGroup.tags.contains(it) } ?: true
            val byFavorite = (!filterFavorite || mGroup.favorite)

            byGroup && byAntiTags && byTags && byFavorite
        }
}