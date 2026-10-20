package com.thekeeperofpie.artistalleydatabase.alley.changelog.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.changelog.ChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.changelog.StampRallyChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.changelog.sortArtistsForChangelog
import com.thekeeperofpie.artistalleydatabase.alley.changelog.sortRalliesForChangelog
import com.thekeeperofpie.artistalleydatabase.alley.changelog.toChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.transform.transform
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalDate

class FavoritesChangelogUseCase<Input>(
    artistEntryDao: ArtistEntryDao,
    stampRallyEntryDao: StampRallyEntryDao,
    @Assisted scope: CoroutineScope,
    @Assisted randomSeed: Int,
    @Assisted dataYear: DataYear,
    @Assisted input: () -> Input,
    @Assisted private val filterArtist: ChangelogEntry.Artist.(Input) -> Boolean,
    @Assisted private val filterRally: StampRallyChangelogEntry.(Input) -> Boolean,
) {
    private val changelog by transform(scope) {
        produceState(emptyList(), dataYear, randomSeed) {
            val artists = artistEntryDao.getChangelog(dataYear, false)
                .map(ChangelogEntry::Artist)
            val allRallies = stampRallyEntryDao.getAllEntriesForChangelog(dataYear)
            val stampRallies = stampRallyEntryDao.getChangelog(dataYear)
                .mapNotNull { it.toChangelogEntry(dataYear, allRallies, randomSeed) }
                .map(ChangelogEntry::StampRally)
            value = (artists + stampRallies)
                .groupBy { it.date }
                .toList()
                .sortedByDescending { it.first }
        }.value
    }

    internal val changes by transform(scope) {
        val changelog = changelog
        val input = input()
        val changes = changelog.mapNotNull {
            val artists = it.second.filterIsInstance<ChangelogEntry.Artist>()
            val rallies = it.second.filterIsInstance<ChangelogEntry.StampRally>()
                .map { it.stampRally }
            val [addedArtists, updatedArtists] =
                artists.filterArtists(input)
                    .partition { it.artist.isBrandNew }
            val [addedRallies, updatedRallies] = rallies.filterRallies(input)
                .partition { it.isBrandNew }
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
            input = input,
        )
    }

    private fun List<ChangelogEntry.Artist>.filterArtists(input: Input) =
        filter { it.filterArtist(input) }

    private fun List<StampRallyChangelogEntry>.filterRallies(input: Input) =
        filter { it.filterRally(input) }

    data class Changes<Input>(
        val changes: List<Change>,
        val input: Input,
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


    @Inject
    class Factory(
        private val artistEntryDao: ArtistEntryDao,
        private val stampRallyEntryDao: StampRallyEntryDao,
    ) {
        fun <Input> create(
            @Assisted scope: CoroutineScope,
            @Assisted dataYear: DataYear,
            @Assisted randomSeed: Int,
            @Assisted input: () -> Input,
            @Assisted filterArtist: ChangelogEntry.Artist.(Input) -> Boolean,
            @Assisted filterRally: StampRallyChangelogEntry.(Input) -> Boolean,
        ) = FavoritesChangelogUseCase(
            artistEntryDao = artistEntryDao,
            stampRallyEntryDao = stampRallyEntryDao,
            scope = scope,
            dataYear = dataYear,
            randomSeed = randomSeed,
            input = input,
            filterArtist = filterArtist,
            filterRally = filterRally,
        )
    }
}
