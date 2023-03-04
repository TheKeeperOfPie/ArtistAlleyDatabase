package com.thekeeperofpie.artistalleydatabase.anilist

import android.util.Log
import androidx.annotation.WorkerThread
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.android_utils.nullable
import com.thekeeperofpie.artistalleydatabase.android_utils.split
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection.MultiText.Entry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class AniListAutocompleter @Inject constructor(
    private val aniListJson: AniListJson,
    private val aniListApi: AniListApi,
    private val characterRepository: CharacterRepository,
    private val mediaRepository: MediaRepository,
    private val aniListDataConverter: AniListDataConverter,
) {

    companion object {
        private const val TAG = "AniListAutocompleter"
    }

    private fun <DataType : Operation.Data, ResponseType : ApolloResponse<DataType>> aniListCall(
        apiCall: () -> Flow<ResponseType>,
        transform: suspend (DataType) -> List<Entry?>,
    ) = apiCall()
        .nullable()
        .catch { Log.e(TAG, "Failed to search", it); emit(null) }
        .mapNotNull { it?.data }
        .map(transform)
        .map { it.filterNotNull() }
        .startWith(item = emptyList())

    suspend fun querySeriesLocal(
        query: String,
        queryLocal: suspend (query: String) -> List<String>
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

    fun querySeriesNetwork(query: String): Flow<List<Entry>> {
        val search = aniListCall({ aniListApi.searchSeries(query) }) {
            it.Page.media
                .mapNotNull { it?.aniListMedia }
                .map(aniListDataConverter::seriesEntry)
        }

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
        queryLocal: suspend (query: String) -> List<String>
    ): Flow<Pair<List<Entry>, List<Entry>>> {
        val local = combine(queryCharactersLocal(query, queryLocal)) { it.toList() }
        val network = if (query.isBlank()) flowOf(emptyList()) else queryCharactersNetwork(query)
        return combine(local, network) { localValue, networkValue ->
            val localValueNotNull = localValue.filterNotNull()
            val (first, second) = localValueNotNull.map { localEntry ->
                networkValue.firstOrNull { networkEntry -> localEntry.id == networkEntry.id }
                    ?: localEntry
            }
                .split { it.text.contains(query, ignoreCase = true) }

            val filteredNetwork = networkValue.toMutableList().apply {
                removeAll { networkEntry ->
                    localValueNotNull.any { localEntry -> localEntry.id == networkEntry.id }
                }
            }

            first to filteredNetwork + second
        }
    }

    private suspend fun queryCharactersLocal(
        query: String,
        queryLocal: suspend (query: String) -> List<String>
    ) = queryLocal(query).map {
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

    private fun queryCharactersNetwork(
        query: String
    ): Flow<List<Entry>> {
        val search = aniListCall({ aniListApi.searchCharacters(query) }) {
            it.Page.characters.filterNotNull()
                .map { aniListDataConverter.characterEntry(it.aniListCharacter) }
        }

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
        characterValue: StateFlow<String>,
        queryCharactersLocal: suspend (query: String) -> List<String>,
    ): Flow<List<Entry>> {
        @Suppress("OPT_IN_USAGE")
        return characterLockState
            .debounce(2.seconds)
            .flatMapLatest {
                if (it == EntrySection.LockState.LOCKED) {
                    flowOf(emptyList())
                } else {
                    combine(
                        characterValue,
                        seriesContents.map { it.filterIsInstance<Entry.Prefilled<*>>() }
                            .map { it.mapNotNull(AniListUtils::mediaId) }
                            .distinctUntilChanged()
                            .flatMapLatest {
                                // For entries with multiple series, ignore predictions by series
                                // since it can flood the predictions with unuseful results
                                if (it.size > 1) return@flatMapLatest emptyFlow()
                                it.map {
                                    aniListApi.charactersByMedia(it)
                                        .map { it.map { aniListDataConverter.characterEntry((it)) } }
                                        .catch {}
                                        .startWith(item = emptyList())
                                }
                                    .let {
                                        combine(it) {
                                            it.fold(mutableListOf<Entry>()) { list, value ->
                                                list.apply { addAll(value) }
                                            }
                                        }
                                    }
                            }
                            .startWith(item = emptyList()),
                        characterValue
                            .debounce(2.seconds)
                            .flatMapLatest { query ->
                                queryCharacters(query, queryCharactersLocal)
                            }
                            .startWith(flowOf(emptyList<Entry>() to emptyList()))
                    ) { query, series, (charactersFirst, charactersSecond) ->
                        val (seriesFirst, seriesSecond) = series.toMutableList().apply {
                            removeAll { seriesCharacter ->
                                charactersFirst
                                    .any { character ->
                                        val seriesCharacterEntry =
                                            seriesCharacter as? Entry.Prefilled<*>
                                        if (seriesCharacterEntry != null) {
                                            AniListUtils.characterId(seriesCharacterEntry) ==
                                                    AniListUtils.characterId(character)
                                        } else {
                                            false
                                        }
                                    } || charactersSecond
                                    .any { character ->
                                        val seriesCharacterEntry =
                                            seriesCharacter as? Entry.Prefilled<*>
                                        if (seriesCharacterEntry != null) {
                                            AniListUtils.characterId(seriesCharacterEntry) ==
                                                    AniListUtils.characterId(character)
                                        } else {
                                            false
                                        }
                                    }
                            }
                        }
                            .split { it.text.contains(query, ignoreCase = true) }
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