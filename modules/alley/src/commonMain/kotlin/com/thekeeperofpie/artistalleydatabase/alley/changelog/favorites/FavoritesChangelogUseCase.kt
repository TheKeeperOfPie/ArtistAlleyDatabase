package com.thekeeperofpie.artistalleydatabase.alley.changelog.favorites

import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.changelog.ChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.changelog.StampRallyChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.changelog.sortArtistsForChangelog
import com.thekeeperofpie.artistalleydatabase.alley.changelog.sortRalliesForChangelog
import com.thekeeperofpie.artistalleydatabase.alley.changelog.toChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.LocalDate

@AssistedInject
class FavoritesChangelogUseCase(
    artistEntryDao: ArtistEntryDao,
    stampRallyEntryDao: StampRallyEntryDao,
    userEntryDao: UserEntryDao,
    @Assisted dataYear: DataYear,
    @Assisted showOnlyConfirmedTags: Flow<Boolean>,
    @Assisted private val filterArtist: ChangelogEntry.Artist.(showOnlyConfirmedTags: Boolean, favorites: TagFavorites) -> Boolean,
    @Assisted private val filterRally: StampRallyChangelogEntry.(favorites: TagFavorites) -> Boolean,
) {
    private val favorites = userEntryDao.getTagFavorites()
        .mapLatest {
            TagFavorites(seriesIds = it.seriesIds.toSet(), merchIds = it.merchIds.toSet())
        }

    private val changelog = flowFromSuspend {
        val artists = artistEntryDao.getChangelog(false)
            .map(ChangelogEntry::Artist)
        val allRallies = stampRallyEntryDao.getAllEntriesForChangelog(dataYear)
        val stampRallies = stampRallyEntryDao.getChangelog(dataYear)
            .mapNotNull { it.toChangelogEntry(dataYear, allRallies) }
            .map(ChangelogEntry::StampRally)
        artists + stampRallies
    }
        .mapLatest {
            it.groupBy { it.date }
                .toList()
                .sortedByDescending { it.first }
        }


    internal val changes = combine(changelog, showOnlyConfirmedTags, favorites, ::Triple)
        .mapLatest { (changelog, showOnlyConfirmedTags, favorites) ->
            val changes = changelog.mapNotNull {
                val artists = it.second.filterIsInstance<ChangelogEntry.Artist>()
                val rallies = it.second.filterIsInstance<ChangelogEntry.StampRally>()
                    .map { it.stampRally }
                val (addedArtists, updatedArtists) =
                    artists.filterArtists(showOnlyConfirmedTags, favorites)
                        .partition { it.artist.isBrandNew }
                val (addedRallies, updatedRallies) = rallies.filterRallies(favorites)
                    .partition { it.images.isEmpty() }
                if (addedArtists.isEmpty() &&
                    updatedArtists.isEmpty() &&
                    addedRallies.isEmpty() &&
                    updatedRallies.isEmpty()
                ) {
                    return@mapNotNull null
                }
                Change(
                    date = it.first,
                    addedArtists = addedArtists.sortArtistsForChangelog(),
                    updatedArtists = updatedArtists.sortArtistsForChangelog(),
                    addedRallies = addedRallies.sortRalliesForChangelog(),
                    updatedRallies = updatedRallies.sortRalliesForChangelog(),
                )
            }
            Changes(
                changes = changes,
                showOnlyConfirmedTags = showOnlyConfirmedTags,
                favorites = favorites,
            )
        }

    private fun List<ChangelogEntry.Artist>.filterArtists(
        showOnlyConfirmedTags: Boolean,
        favorites: TagFavorites,
    ) = filter { it.filterArtist(showOnlyConfirmedTags, favorites) }

    private fun List<StampRallyChangelogEntry>.filterRallies(favorites: TagFavorites) =
        filter { it.filterRally(favorites) }

    data class Changes(
        val changes: List<Change>,
        val showOnlyConfirmedTags: Boolean,
        val favorites: TagFavorites,
    )

    data class Change(
        val date: LocalDate,
        val addedArtists: List<ChangelogEntry.Artist>,
        val updatedArtists: List<ChangelogEntry.Artist>,
        val addedRallies: List<StampRallyChangelogEntry>,
        val updatedRallies: List<StampRallyChangelogEntry>,
    )

    data class TagFavorites(
        val seriesIds: Set<String>,
        val merchIds: Set<String>,
    )
}
