package com.thekeeperofpie.artistalleydatabase.autocomplete

import android.util.Log
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDataConverter
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils.nullable
import com.thekeeperofpie.artistalleydatabase.utils.split
import com.thekeeperofpie.artistalleydatabase.utils.start
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class Autocompleter @Inject constructor(
    private val artEntryDao: ArtEntryDetailsDao,
    private val appMoshi: AppMoshi,
    private val aniListApi: AniListApi,
    private val characterRepository: CharacterRepository,
    private val mediaRepository: MediaRepository,
    private val artEntryDataConverter: ArtEntryDataConverter,
) {

    companion object {
        private const val TAG = "Autocompleter"
    }

    suspend fun querySeriesLocal(query: String) =
        artEntryDao.querySeries(query)
            .map {
                val either = appMoshi.parseSeriesColumn(it)
                if (either is Either.Right) {
                    mediaRepository.getEntry(either.value.id)
                        .filterNotNull()
                        .map(artEntryDataConverter::seriesEntry)
                        .start(artEntryDataConverter.seriesEntry(either.value))
                } else {
                    flowOf(Entry.Custom(it))
                }
            }
            .ifEmpty { listOf(flowOf(null)) }

    fun querySeriesNetwork(query: String) =
        aniListCall({ aniListApi.searchSeries(query) }) {
            it.Page.media
                .mapNotNull { it?.aniListMedia }
                .map(artEntryDataConverter::seriesEntry)
        }

    suspend fun queryCharacters(query: String): Flow<Pair<List<Entry>, List<Entry>>> {
        val local = combine(queryCharactersLocal(query)) { it.toList() }
        val network = if (query.isBlank()) flowOf(emptyList()) else queryCharactersNetwork(query)
        return combine(local, network) { localValue, networkValue ->
            val localValueNotNull = localValue.filterNotNull()
            val (first, second) = localValueNotNull.map { localEntry ->
                    networkValue.firstOrNull { networkEntry ->
                        (localEntry as? Entry.Prefilled)?.id ==
                                networkEntry.id
                    } ?: localEntry
                }
                .split { it.text.contains(query, ignoreCase = true) }

            val filteredNetwork = networkValue.toMutableList().apply {
                removeAll { networkEntry ->
                    localValueNotNull.any { localEntry ->
                        (localEntry as? Entry.Prefilled)?.id == networkEntry.id
                    }
                }
            }

            first to filteredNetwork + second
        }
    }

    private suspend fun queryCharactersLocal(query: String) =
        artEntryDao.queryCharacters(query)
            .map {
                val either = appMoshi.parseCharacterColumn(it)
                if (either is Either.Right) {
                    characterRepository.getEntry(either.value.id)
                        .filterNotNull()
                        .flatMapLatest { character ->
                            // TODO: Batch query?
                            character.mediaIds
                                ?.map { mediaRepository.getEntry(it) }
                                ?.let { combine(it) { it.toList() } }
                                .let { it ?: flowOf(listOf(null)) }
                                .map { character to it.filterNotNull() }
                        }
                        .map { artEntryDataConverter.characterEntry(it.first, it.second) }
                        .start(artEntryDataConverter.characterEntry(either.value))
                        .filterNotNull()
                } else {
                    flowOf(Entry.Custom(it))
                }
            }
            .ifEmpty { listOf(flowOf(null)) }

    private fun queryCharactersNetwork(
        query: String
    ): Flow<List<Entry.Prefilled>> {
        val search = aniListCall({ aniListApi.searchCharacters(query) }) {
            it.Page.characters.filterNotNull()
                .map { artEntryDataConverter.characterEntry(it.aniListCharacter) }
        }
        val queryAsId = query.toIntOrNull()
        return if (queryAsId == null) search else {
            combine(search, aniListApi.getCharacter(queryAsId)
                .catch {}
                .mapNotNull { it?.aniListCharacter }
                .mapNotNull(artEntryDataConverter::characterEntry)
                .map(::listOf)
                .start(emptyList())
            ) { result, character -> result + character }
        }
    }

    private fun <DataType : Operation.Data, ResponseType : ApolloResponse<DataType>> aniListCall(
        apiCall: () -> Flow<ResponseType>,
        transform: suspend (DataType) -> List<Entry.Prefilled?>,
    ) = apiCall()
        .nullable()
        .catch { Log.e(TAG, "Failed to search", it); emit(null) }
        .mapNotNull { it?.data }
        .map(transform)
        .map { it.filterNotNull() }
        .start(emptyList())
}