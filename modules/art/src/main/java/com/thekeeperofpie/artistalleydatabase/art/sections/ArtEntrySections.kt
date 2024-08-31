package com.thekeeperofpie.artistalleydatabase.art.sections

import com.hoc081098.flowext.withLatestFrom
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.ArtNavDestinations
import com.thekeeperofpie.artistalleydatabase.art.R
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class ArtEntrySections(defaultLockState: EntrySection.LockState = EntrySection.LockState.UNLOCKED) {

    val seriesSection = EntrySection.MultiText(
        R.string.art_entry_series_header_zero,
        R.string.art_entry_series_header_one,
        R.string.art_entry_series_header_many,
        lockState = defaultLockState,
        navRoute = {
            ArtNavDestinations.BROWSE_SELECTION.id +
                    "?queryType=${ArtEntryColumn.SERIES.name}" +
                    "&title=${it.text}" +
                    "&queryString=${it.text}"
        }
    )

    val characterSection = EntrySection.MultiText(
        R.string.art_entry_characters_header_zero,
        R.string.art_entry_characters_header_one,
        R.string.art_entry_characters_header_many,
        lockState = defaultLockState,
        navRoute = {
            ArtNavDestinations.BROWSE_SELECTION.id +
                    "?queryType=${ArtEntryColumn.CHARACTERS.name}" +
                    "&title=${it.text}" +
                    "&queryString=${it.text}"
        }
    )

    val sourceSection = SourceDropdown(locked = defaultLockState)

    val artistSection = EntrySection.MultiText(
        R.string.art_entry_artists_header_zero,
        R.string.art_entry_artists_header_one,
        R.string.art_entry_artists_header_many,
        lockState = defaultLockState,
        navRoute = {
            ArtNavDestinations.BROWSE_SELECTION.id +
                    "?queryType=${ArtEntryColumn.ARTISTS.name}" +
                    "&title=${it.text}" +
                    "&queryString=${it.text}"
        }
    )

    val tagSection = EntrySection.MultiText(
        R.string.art_entry_tags_header_zero,
        R.string.art_entry_tags_header_one,
        R.string.art_entry_tags_header_many,
        lockState = defaultLockState,
        navRoute = {
            ArtNavDestinations.BROWSE_SELECTION.id +
                    "?queryType=${ArtEntryColumn.TAGS.name}" +
                    "&title=${it.text}" +
                    "&queryString=${it.text}"
        }
    )

    val printSizeSection = PrintSizeDropdown(lockState = defaultLockState)

    val notesSection = EntrySection.LongText(
        headerRes = R.string.art_entry_notes_header,
        lockState = defaultLockState
    )

    val sections = listOf(
        seriesSection,
        characterSection,
        artistSection,
        sourceSection,
        tagSection,
        printSizeSection,
        notesSection,
    )

    fun subscribeSectionPredictions(
        scope: CoroutineScope,
        artEntryDao: ArtEntryDetailsDao,
        dataConverter: DataConverter,
        mediaRepository: MediaRepository,
        characterRepository: CharacterRepository,
        aniListAutocompleter: AniListAutocompleter,
        appJson: AppJson,
    ) {
        scope.launch(CustomDispatchers.IO) {
            artistSection.subscribePredictions(
                localCall = {
                    artEntryDao.queryArtists(it)
                        .map(EntrySection.MultiText.Entry::Custom)
                        .map { flowOf(it) }
                        .ifEmpty { listOf(flowOf(null)) }
                }
            )
        }
        scope.launch(CustomDispatchers.IO) {
            seriesSection.subscribePredictions(
                localCall = {
                    aniListAutocompleter.querySeriesLocal(it, artEntryDao::querySeries)
                },
                networkCall = aniListAutocompleter::querySeriesNetwork
            )
        }
        scope.launch(CustomDispatchers.IO) {
            aniListAutocompleter.characterPredictions(
                characterSection.lockStateFlow,
                seriesSection.contentUpdates(),
                characterSection.valueUpdates(),
            ) { artEntryDao.queryCharacters(it) }
                .collectLatest {
                    withContext(CustomDispatchers.Main) {
                        characterSection.predictions = it.toMutableList()
                    }
                }
        }
        scope.launch(CustomDispatchers.IO) {
            tagSection.subscribePredictions(
                localCall = {
                    artEntryDao.queryTags(it)
                        .map(EntrySection.MultiText.Entry::Custom)
                        .map { flowOf(it) }
                        .ifEmpty { listOf(flowOf(null)) }
                }
            )
        }

        scope.launch(CustomDispatchers.IO) {
            artistSection.contentUpdates()
                .withLatestFrom(
                    combine(
                        sourceSection.conventionSectionItem.updates(),
                        sourceSection.lockStateFlow,
                        ::Pair
                    )
                ) { artist, (convention, lock) -> Triple(artist, convention, lock) }
                .flatMapLatest {
                    // flatMapLatest to immediately drop request if lockState has changed
                    flowOf(it)
                        .filter { (_, _, lockState) ->
                            when (lockState) {
                                EntrySection.LockState.LOCKED -> false
                                EntrySection.LockState.UNLOCKED,
                                EntrySection.LockState.DIFFERENT,
                                null,
                                -> true
                            }
                        }
                        .filter { (_, convention, _) ->
                            convention.name.isNotEmpty()
                                    && convention.year != null && convention.year > 1000
                                    && (convention.hall.isEmpty() || convention.booth.isEmpty())
                        }
                        .mapNotNull { (artistEntries, convention) ->
                            artistEntries.firstNotNullOfOrNull {
                                artEntryDao
                                    .queryArtistForHallBooth(
                                        it.searchableValue,
                                        convention.name,
                                        convention.year!!
                                    )
                                    .takeUnless { it.isNullOrBlank() }
                                    ?.let<String, SourceType.Convention>(
                                        appJson.json::decodeFromString
                                    )
                                    ?.takeIf {
                                        it.name == convention.name && it.year == convention.year
                                    }
                            }
                        }
                }
                .collectLatest {
                    withContext(CustomDispatchers.Main) {
                        val sectionChanged = sourceSection.conventionSectionItem
                            .updateHallBoothIfEmpty(
                                expectedName = it.name,
                                expectedYear = it.year!!,
                                newHall = it.hall,
                                newBooth = it.booth
                            )
                        if (sectionChanged) {
                            sourceSection.lockIfUnlocked()
                        }

                        artistSection.lockIfUnlocked()
                    }
                }
        }
    }
}
