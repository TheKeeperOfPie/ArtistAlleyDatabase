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
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormSection
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
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
            querySeriesNetwork2(query),
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
                .startWith(aniListDataConverter.seriesEntry2(either.value))
        } else {
            flowOf(EntryFormSection.MultiText.Entry.Custom(it.trim()))
        }
    }

    fun querySeriesNetwork2(
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
}
