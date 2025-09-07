package com.thekeeperofpie.artistalleydatabase.anilist

import androidx.collection.LruCache
import androidx.compose.runtime.snapshotFlow
import co.touchlab.kermit.Logger
import com.anilist.data.fragment.AniListCharacter
import com.anilist.data.fragment.AniListMedia
import com.anilist.data.type.MediaType
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormSection
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@SingletonScope
@Inject
class AniListAutocompleter2(
    private val aniListJson: AniListJson,
    private val aniListApi: AniListApi,
    private val characterRepository: CharacterRepository,
    private val dispatchers: CustomDispatchers,
    private val mediaRepository: MediaRepository,
    private val aniListDataConverter: AniListDataConverter,
) {

    companion object {
        private const val TAG = "AniListAutocompleter"
    }

    private val charactersBySeriesCache =
        LruCache<String, List<EntryFormSection.MultiText.Entry.Prefilled<AniListCharacter>>>(50)
    private val charactersBySeriesKnownEmpty = LruCache<String, Unit>(1000)
    private val charactersNetworkCache =
        LruCache<String, List<EntryFormSection.MultiText.Entry.Prefilled<AniListCharacter>>>(50)
    private val charactersNetworkKnownEmpty = LruCache<String, Unit>(1000)

    suspend fun series(
        query: String,
        queryLocal: suspend (query: String) -> List<String>,
    ): Flow<List<EntryFormSection.MultiText.Entry>> =
        combine(
            // TODO: Flatten this
            combine(
                querySeriesLocal(query, queryLocal),
                Array<EntryFormSection.MultiText.Entry>::toList,
            ).startWith(item = emptyList()),
            querySeriesNetwork(query),
            List<EntryFormSection.MultiText.Entry>::plus,
        )
            .flowOn(dispatchers.io)

    suspend fun querySeriesLocal(
        query: String,
        queryLocal: suspend (query: String) -> List<String>,
    ) = queryLocal(query).map {
        val either = aniListJson.parseSeriesColumn(it)
        if (either is Either.Right) {
            mediaRepository.getEntry(either.value.id)
                .filterNotNull()
                .map(aniListDataConverter::seriesEntry2)
                .startWith(item = aniListDataConverter.seriesEntry2(either.value))
        } else {
            flowOf(EntryFormSection.MultiText.Entry.Custom(it.trim()))
        }
    }

    fun querySeriesNetwork(
        query: String,
        type: MediaType? = null,
    ): Flow<List<EntryFormSection.MultiText.Entry.Prefilled<AniListMedia>>> {
        val search = aniListApi.searchSeries(query, type).mapNotNull {
            it?.page?.media
                ?.filterNotNull()
                ?.map(aniListDataConverter::seriesEntry2)
        }
            .catch { Logger.e(TAG, it) { "Failed to search" } }
            .startWith(item = emptyList())

        // AniList IDs are integers
        val queryAsId = query.toIntOrNull()
        return if (queryAsId == null) search else {
            val fetchById = flow { emit(aniListApi.getMedia(query)) }
                .catch {}
                .filterNotNull()
                .mapNotNull(aniListDataConverter::seriesEntry2)
                .map(::listOf)
                .startWith(item = emptyList())
            combine(fetchById, search) { result, series -> result + series }
        }
    }

    private suspend fun queryEntryCharactersLocal(
        query: String,
        queryEntryLocal: suspend (query: String) -> List<String>,
    ): Flow<List<EntryFormSection.MultiText.Entry>> {
        if (query.isBlank()) return flowOf(emptyList())
        return combine(
            queryEntryLocal(query).map {
                val either = aniListJson.parseCharacterColumn(it)
                if (either is Either.Right) {
                    characterRepository.getEntry(either.value.id)
                        .filterNotNull()
                        .map(aniListDataConverter::characterEntry2)
                        .startWith(item = aniListDataConverter.characterEntry2(either.value))
                        .filterNotNull()
                } else {
                    flowOf(EntryFormSection.MultiText.Entry.Custom(it))
                }
            }.ifEmpty { listOf(flowOf(null)) }
        ) { it.toList() }
            .mapLatest { it.filterNotNull() }
            .startWith(emptyList())
    }

    private fun queryCharactersLocal(query: String): Flow<List<EntryFormSection.MultiText.Entry.Prefilled<CharacterEntry>>> {
        if (query.isBlank()) return flowOf(emptyList())
        return characterRepository.search(query)
            .map {
                it.map(aniListDataConverter::characterEntry2)
            }
            .startWith(item = emptyList())
    }

    private fun queryCharactersNetwork(
        query: String,
    ): Flow<List<EntryFormSection.MultiText.Entry.Prefilled<*>>> {
        if (query.isBlank()) return flowOf(emptyList())
        val cached = charactersNetworkCache[query]
        if (cached != null) return flowOf(cached)
        if (charactersNetworkKnownEmpty[query] != null) return flowOf(emptyList())
        return aniListApi.searchCharacters(query)
            .mapNotNull {
                it?.page?.characters
                    ?.filterNotNull()
                    ?.map(aniListDataConverter::characterEntry2)
            }
            .catch { Logger.e(TAG, it) { "Failed to search" } }
            .onEach {
                if (it.isEmpty()) {
                    charactersNetworkKnownEmpty.put(query, Unit)
                } else {
                    charactersNetworkCache.put(query, it)
                }
            }
            .startWith(item = emptyList())
    }

    private fun queryCharactersNetworkQueryAsId(
        query: String,
    ): Flow<List<EntryFormSection.MultiText.Entry.Prefilled<*>>> {
        // AniList IDs are integers
        val queryAsId = query.toIntOrNull()
        return if (queryAsId == null) {
            flowOf(emptyList())
        } else {
            flow { emit(aniListApi.getCharacter(queryAsId.toString())) }
                .catch {}
                .filterNotNull()
                .mapNotNull(aniListDataConverter::characterEntry2)
                .map(::listOf)
                .startWith(item = emptyList())
        }
    }

    fun charactersBySeries(mediaId: String): Flow<List<EntryFormSection.MultiText.Entry.Prefilled<AniListCharacter>>> {
        val cached = charactersBySeriesCache[mediaId]
        if (cached != null) return flowOf(cached)
        if (charactersBySeriesKnownEmpty[mediaId] != null) return flowOf(emptyList())
        return aniListApi.charactersByMedia(mediaId)
            .map { it.map { aniListDataConverter.characterEntry2(it) } }
            .catch {
                Logger.d(TAG, it) {
                    "Error loading characters by media ID $mediaId"
                }
            }
            .onEach {
                if (it.isEmpty()) {
                    charactersBySeriesKnownEmpty.put(mediaId, Unit)
                } else {
                    charactersBySeriesCache.put(mediaId, it)
                }
            }
            .startWith(item = emptyList())
    }

    fun combineCharacters(
        query: String,
        bySeries: List<EntryFormSection.MultiText.Entry.Prefilled<*>>,
        entry: List<EntryFormSection.MultiText.Entry>,
        network: List<EntryFormSection.MultiText.Entry.Prefilled<*>>,
        networkAsId: List<EntryFormSection.MultiText.Entry.Prefilled<*>>,
        local: List<EntryFormSection.MultiText.Entry.Prefilled<*>>,
    ): List<EntryFormSection.MultiText.Entry> {
        // Sorts values by entryResult, localResult, networkResult,
        // while overwriting with more specific objects in the latter lists
        val tempMap = LinkedHashMap<String, EntryFormSection.MultiText.Entry>()
        networkAsId.forEach { tempMap[it.id] = it }
        entry.forEach { tempMap[it.id] = it }
        local.forEach { tempMap[it.id] = it }
        network.forEach { tempMap[it.id] = it }
        val (first, second) = tempMap.values
            .partition { it.text.contains(query, ignoreCase = true) }
        val (bySeriesFirst, bySeriesSecond) = bySeries.toMutableList()
            .partition { it.text.contains(query, ignoreCase = true) }
        return (bySeriesFirst + first + bySeriesSecond + second).distinctBy { it.id }
    }

    fun characters(
        charactersState: EntryFormSection.MultiText,
        seriesState: EntryFormSection.MultiText,
        entryCharactersLocal: suspend (query: String) -> List<String>,
    ) = snapshotFlow { charactersState.lockState }
        .flatMapLatest { lockState ->
            if (lockState == EntryFormSection.LockState.LOCKED) {
                emptyFlow()
            } else {
                val charactersBySeries = snapshotFlow { seriesState.content.toList() }
                    .mapLatest {
                        it.filterIsInstance<EntryFormSection.MultiText.Entry.Prefilled<*>>()
                            .mapNotNull(AniListUtils::mediaId)
                            .singleOrNull()
                    }
                    .distinctUntilChanged()
                    .flatMapLatest {
                        if (it == null) {
                            flowOf(emptyList())
                        } else {
                            charactersBySeries(it)
                        }
                    }
                    .startWith(item = emptyList())

                val characters =
                    snapshotFlow { charactersState.pendingFocused to charactersState.pendingNewValue.text }
                        .debounce(500.milliseconds)
                        .filter { it.first }
                        .flatMapLatest { (_, query) ->
                            combine(
                                queryEntryCharactersLocal(query, entryCharactersLocal),
                                queryCharactersNetwork(query),
                                queryCharactersNetworkQueryAsId(query),
                                queryCharactersLocal(query),
                            ) { entry, network, networkAsId, local ->
                                CharactersQueryResult(
                                    query = query,
                                    entry = entry,
                                    network = network,
                                    networkAsId = networkAsId,
                                    local = local,
                                )
                            }
                        }
                        .startWith(item = CharactersQueryResult())

                combine(charactersBySeries, characters) { charactersBySeries, characters ->
                    val (query, entry, network, networkAsId, local) = characters
                    combineCharacters(query, charactersBySeries, entry, network, networkAsId, local)
                }
            }
        }

    private data class CharactersQueryResult(
        val query: String = "",
        val entry: List<EntryFormSection.MultiText.Entry> = emptyList(),
        val network: List<EntryFormSection.MultiText.Entry.Prefilled<*>> = emptyList(),
        val networkAsId: List<EntryFormSection.MultiText.Entry.Prefilled<*>> = emptyList(),
        val local: List<EntryFormSection.MultiText.Entry.Prefilled<*>> = emptyList(),
    )
}
