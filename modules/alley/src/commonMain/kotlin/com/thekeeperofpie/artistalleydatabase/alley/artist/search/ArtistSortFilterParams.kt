package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesRowId
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CommissionType
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link

data class ArtistSortFilterParams(
    val sortOption: ArtistSearchSortOption,
    val sortAscending: Boolean,
    val seriesIn: Set<SeriesRowId>,
    val merchIn: Set<String>,
    val commissionsIn: Set<CommissionType>,
    val linkTypesIn: Set<Link.Type>,
    val exhibitorTagsIn: Set<String>,
    val artistTagsIn: Set<ArtistTag>,
    val artistTagsNotIn: Set<ArtistTag>,
    val showOnlyConfirmedTags: Boolean,
    val showOutdatedCatalogs: Boolean,
    val hideFavorited: Boolean,
    val hideIgnored: Boolean,
) {
    companion object {
        fun unfiltered(
            lockedSeriesIn: SeriesRowId? = null,
            lockedMerchIn: String? = null,
        ) = ArtistSortFilterParams(
            sortOption = ArtistSearchSortOption.BOOTH,
            sortAscending = true,
            seriesIn = setOfNotNull(lockedSeriesIn),
            merchIn = setOfNotNull(lockedMerchIn),
            commissionsIn = emptySet(),
            linkTypesIn = emptySet(),
            exhibitorTagsIn = emptySet(),
            artistTagsIn = emptySet(),
            artistTagsNotIn = emptySet(),
            showOnlyConfirmedTags = false,
            showOutdatedCatalogs = false,
            hideFavorited = false,
            hideIgnored = false,
        )
    }
}
