package com.thekeeperofpie.artistalleydatabase.anilist

import androidx.annotation.WorkerThread
import co.touchlab.kermit.Logger
import com.anilist.data.fragment.AniListMedia
import com.anilist.data.type.MediaType
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.utils.Either
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@SingleIn(AppScope::class)
@Inject
class AniListAutocompleter(
    private val aniListJson: AniListJson,
    private val aniListApi: AniListApi,
    private val characterRepository: CharacterRepository,
    private val mediaRepository: MediaRepository,
    private val aniListDataConverter: AniListDataConverter,
) {

    companion object {
        private const val TAG = "AniListAutocompleter"
    }

    suspend fun querySeriesLocal(
        query: String,
        queryLocal: suspend (query: String) -> List<String>,
    ) = queryLocal(query).map {
        val either = aniListJson.parseSeriesColumn(it)
        if (either is Either.Right) {
            mediaRepository.getEntry(either.value.id)
                .filterNotNull()
                .map(aniListDataConverter::seriesEntry)
                .startWith(aniListDataConverter.seriesEntry(either.value))
        } else {
            flowOf(Entry.Custom(it))
        }
    }

    fun querySeriesNetwork(
        query: String,
        type: MediaType? = null,
    ): Flow<List<Entry.Prefilled<AniListMedia>>> {
        val search = aniListApi.searchSeries(query, type).mapNotNull {
            it?.page?.media
                ?.filterNotNull()
                ?.map(aniListDataConverter::seriesEntry)
        }
            .catch { Logger.e(TAG, it) { "Failed to search" } }
            .startWith(item = emptyList())

        // AniList IDs are integers
        val queryAsId = query.toIntOrNull()
        return if (queryAsId == null) search else {
            val fetchById = flow { emit(aniListApi.getMedia(query)) }
                .catch {}
                .filterNotNull()
                .mapNotNull(aniListDataConverter::seriesEntry)
                .map(::listOf)
                .startWith(item = emptyList())
            combine(fetchById, search) { result, series -> result + series }
        }
    }

    private suspend fun queryCharacters(
        query: String,
        queryLocal: suspend (query: String) -> List<String>,
    ): Flow<Pair<List<Entry>, List<Entry>>> {
        val entryCharactersLocal =
            combine(queryEntryCharactersLocal(query, queryLocal)) { it.toList() }
        val local = queryCharactersLocal(query)
        val network = if (query.isBlank()) flowOf(emptyList()) else queryCharactersNetwork(query)
        return combine(
            entryCharactersLocal,
            local,
            network
        ) { entryResult, localResult, networkResult ->
            // Sorts values by entryResult, localResult, networkResult,
            // while overwriting with more specific objects in the latter lists
            val tempMap = LinkedHashMap<String, Entry>()
            entryResult.filterNotNull().forEach { tempMap[it.id] = it }
            localResult.forEach { tempMap[it.id] = it }
            networkResult.forEach { tempMap[it.id] = it }
            tempMap.values.partition { it.text.contains(query, ignoreCase = true) }
        }
    }

    private suspend fun queryEntryCharactersLocal(
        query: String,
        queryEntryLocal: suspend (query: String) -> List<String>,
    ) = queryEntryLocal(query).map {
        val either = aniListJson.parseCharacterColumn(it)
        if (either is Either.Right) {
            characterRepository.getEntry(either.value.id)
                .filterNotNull()
                .map(aniListDataConverter::characterEntry)
                .startWith(aniListDataConverter.characterEntry(either.value))
                .filterNotNull()
        } else {
            flowOf(Entry.Custom(it))
        }
    }.ifEmpty { listOf(flowOf(null)) }

    private fun queryCharactersLocal(query: String) = if (query.isBlank()) {
        flowOf(emptyList())
    } else {
        characterRepository.search(query)
            .map {
                it.map(aniListDataConverter::characterEntry)
            }
    }

    private fun queryCharactersNetwork(
        query: String,
    ): Flow<List<Entry>> {
        val search = aniListApi.searchCharacters(query).mapNotNull {
            it?.page?.characters
                ?.filterNotNull()
                ?.map(aniListDataConverter::characterEntry)
        }
            .catch { Logger.e(TAG, it) { "Failed to search" } }
            .startWith(item = emptyList())

        // AniList IDs are integers
        val queryAsId = query.toIntOrNull()
        return if (queryAsId == null) search else {
            val fetchById = flow { emit(aniListApi.getCharacter(queryAsId.toString())) }
                .catch {}
                .filterNotNull()
                .mapNotNull(aniListDataConverter::characterEntry)
                .map(::listOf)
                .startWith(item = emptyList())
            combine(fetchById, search) { result, character -> result + character }
        }
    }

    fun characterPredictions(
        characterLockState: StateFlow<EntrySection.LockState?>,
        seriesContents: StateFlow<List<Entry>>,
        query: StateFlow<String>,
        queryEntryCharactersLocal: suspend (query: String) -> List<String>,
    ): Flow<List<Entry>> {
        @Suppress("OPT_IN_USAGE")
        return characterLockState
            .debounce(2.seconds)
            .flatMapLatest {
                if (it == EntrySection.LockState.LOCKED) {
                    flowOf(emptyList())
                } else {
                    combine(
                        query,
                        // Map to isNotBlank and filter to only send the request when something
                        // has been typed. Essentially this is a one-way barrier.
                        query.map { it.isNotBlank() }
                            .filter { it }
                            .distinctUntilChanged()
                            .flatMapLatest {
                                seriesContents.map { it.filterIsInstance<Entry.Prefilled<*>>() }
                                    .map { it.mapNotNull(AniListUtils::mediaId) }
                                    .distinctUntilChanged()
                                    .flatMapLatest {
                                        // For entries with multiple series, ignore predictions by series
                                        // since it can flood the predictions with unuseful results
                                        val mediaId = it.singleOrNull()
                                        if (mediaId == null) emptyFlow() else {
                                            aniListApi.charactersByMedia(mediaId)
                                                .map {
                                                    it.map {
                                                        aniListDataConverter.characterEntry(
                                                            it
                                                        )
                                                    }
                                                }
                                                .catch {
                                                    Logger.d(TAG, it) {
                                                        "Error loading characters by media ID $mediaId"
                                                    }
                                                }
                                                .startWith(item = emptyList())
                                        }
                                    }
                            }
                            .startWith(item = emptyList()),
                        query.flatMapLatest { queryCharacters(it, queryEntryCharactersLocal) }
                            .startWith(item = emptyList<Entry>() to emptyList())
                    ) { query, series, (charactersFirst, charactersSecond) ->
                        val (seriesFirst, seriesSecond) = series.toMutableList()
                            .partition { it.text.contains(query, ignoreCase = true) }
                        (seriesFirst + charactersFirst + seriesSecond + charactersSecond).distinctBy { it.id }
                    }
                }
            }
    }

    @WorkerThread
    suspend fun fillCharacterField(characterId: String) =
        characterRepository.getEntry(characterId)
            .filterNotNull()
            .map(aniListDataConverter::characterEntry)
            .filterNotNull()

    @WorkerThread
    suspend fun fillMediaField(mediaId: String) =
        mediaRepository.getEntry(mediaId)
            .filterNotNull()
            .map(aniListDataConverter::seriesEntry)
}
