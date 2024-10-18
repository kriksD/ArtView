package mediaStorage

import info.group.MediaGroup
import info.media.MediaInfo
import info.media.MediaType
import tag.TagStorage

class Filter {
    private var filterTags: List<String>? = null
    private var filterAntiTags: List<String>? = null
    private var mediaGroup: MediaGroup? = null
    private var filterFavorite: Boolean = false
    private var filterHidden: Boolean = false
    private var filterNonHidden: Boolean = true
    private var filterType: MediaType? = null

    fun tags(tagStorage: TagStorage): Filter {
        this.filterTags = tagStorage.selectedTags
        this.filterAntiTags = tagStorage.selectedAntiTags
        return this
    }

    fun group(mediaGroup: MediaGroup): Filter {
        this.mediaGroup = mediaGroup
        return this
    }

    fun favorite(favorite: Boolean = true): Filter {
        filterFavorite = favorite
        return this
    }

    fun hidden(hidden: Boolean = true): Filter {
        filterHidden = hidden
        return this
    }

    fun nonHidden(nonHidden: Boolean = true): Filter {
        filterNonHidden = nonHidden
        return this
    }

    fun type(type: MediaType?): Filter {
        filterType = type
        return this
    }

    fun filter(mediaList: Collection<MediaInfo>): Collection<MediaInfo> = mediaList
        .filter { mediaInfo ->
            val byGroup = mediaGroup?.mediaIDs?.contains(mediaInfo.id) ?: true
            val byAntiTags = filterAntiTags?.none { mediaInfo.tags.contains(it) } ?: true
            val byTags = filterTags?.all { mediaInfo.tags.contains(it) } ?: true
            val byFavorite = (!filterFavorite || mediaInfo.favorite)
            val byHidden = (!filterHidden || mediaInfo.hidden)
            val byNonHidden = (!filterNonHidden || !mediaInfo.hidden)
            val byType = (filterType == null || mediaInfo.type == filterType)

            byGroup && byAntiTags && byTags && byFavorite && byHidden && byNonHidden && byType
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