package com.thekeeperofpie.artistalleydatabase.anilist

import androidx.annotation.Size
import com.anilist.CharacterByIdQuery
import com.anilist.CharacterByIds10Query
import com.anilist.CharacterByIds15Query
import com.anilist.CharacterByIds2Query
import com.anilist.CharacterByIds3Query
import com.anilist.CharacterByIds4Query
import com.anilist.CharacterByIds5Query
import com.anilist.CharactersSearchQuery
import com.anilist.MediaByIdQuery
import com.anilist.MediaByIds10Query
import com.anilist.MediaByIds15Query
import com.anilist.MediaByIds2Query
import com.anilist.MediaByIds3Query
import com.anilist.MediaByIds4Query
import com.anilist.MediaByIds5Query
import com.anilist.MediaSearchQuery
import com.anilist.MediaWithCharactersQuery
import com.anilist.fragment.AniListCharacter
import com.anilist.fragment.AniListMedia
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.thekeeperofpie.artistalleydatabase.android_utils.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.addLoggingInterceptors
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.chunked
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.splitAtIndex
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import okhttp3.OkHttpClient
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class AniListApi(
    application: ScopedApplication,
    cache: AniListCache,
    networkSettings: NetworkSettings,
) {

    companion object {
        private const val TAG = "AniListApi"
    }

    private val apolloClient = ApolloClient.Builder()
        .serverUrl(AniListUtils.GRAPHQL_API_URL)
        .httpEngine(
            DefaultHttpEngine(
                OkHttpClient.Builder()
                    .cache(cache.cache)
                    .addLoggingInterceptors(TAG, networkSettings)
                    .build()
            )
        )
        .addLoggingInterceptors(TAG, networkSettings)
        .build()

    private data class Request<T>(
        val id: String,
        val result: CompletableDeferred<T?> = CompletableDeferred()
    )

    private val mediaRequestChannel = Channel<Request<AniListMedia>>()
    private val characterRequestChannel = Channel<Request<AniListCharacter>>()

    init {
        @OptIn(ExperimentalCoroutinesApi::class)
        application.scope.launch(CustomDispatchers.IO) {
            mediaRequestChannel.consumeAsFlow()
                .chunked(10, 1.seconds)
                .flatMapMerge { requests ->
                    flow<Pair<Request<AniListMedia>, AniListMedia?>> {
                        getMedias(requests.map { it.id.toInt() })
                            .map { media -> requests.first { it.id.toInt() == media.id } to media }
                            .forEach { emit(it) }
                    }.catch {
                        requests.map { it to null }.forEach { emit(it) }
                    }
                }
                .collectLatest { it.first.result.complete(it.second) }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        application.scope.launch(CustomDispatchers.IO) {
            characterRequestChannel.consumeAsFlow()
                .chunked(10, 1.seconds)
                .flatMapMerge { requests ->
                    flow<Pair<Request<AniListCharacter>, AniListCharacter?>> {
                        getCharacters(requests.map { it.id.toInt() })
                            .map { character ->
                                requests.first { it.id.toInt() == character.id } to character
                            }
                            .forEach { emit(it) }
                    }.catch {
                        requests.map { it to null }.forEach { emit(it) }
                    }
                }
                .collectLatest { it.first.result.complete(it.second) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getMedia(id: String): AniListMedia? {
        val request = Request<AniListMedia>(id)
        mediaRequestChannel.send(request)
        return select {
            request.result.onAwait { it }
            onTimeout(1.minutes) { null }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getCharacter(id: String): AniListCharacter? {
        val request = Request<AniListCharacter>(id)
        characterRequestChannel.send(request)
        return select {
            request.result.onAwait { it }
            onTimeout(1.minutes) { null }
        }
    }

    suspend fun getMedias(ids: List<Int>) = getMultiple(
        ids = ids,
        get1 = ::getMedia1,
        get2 = ::getMedias2,
        get3 = ::getMedias3,
        get4 = ::getMedias4,
        get5 = ::getMedias5,
        get10 = ::getMedias10,
        get15 = ::getMedias15,
    )

    suspend fun getCharacters(ids: List<Int>) = getMultiple(
        ids = ids,
        get1 = ::getCharacter1,
        get2 = ::getCharacters2,
        get3 = ::getCharacters3,
        get4 = ::getCharacters4,
        get5 = ::getCharacters5,
        get10 = ::getCharacters10,
        get15 = ::getCharacters15,
    )

    private suspend fun <T> getMultiple(
        ids: List<Int>,
        get1: suspend (Int) -> T?,
        get2: suspend (List<Int>) -> List<T>,
        get3: suspend (List<Int>) -> List<T>,
        get4: suspend (List<Int>) -> List<T>,
        get5: suspend (List<Int>) -> List<T>,
        get10: suspend (List<Int>) -> List<T>,
        get15: suspend (List<Int>) -> List<T>,
    ) = when {
        ids.isEmpty() -> emptyList()
        ids.size == 15 -> get15(ids)
        else -> {
            val results = mutableListOf<T>()
            var remainingIds = ids
            while (remainingIds.isNotEmpty()) {
                var newRemaining = emptyList<Int>()
                results += when {
                    // TODO: Complexity of query with 15 seems to be too high for backend
//                    remainingIds.size >= 15 -> {
//                        val (first, second) = remainingIds.splitAtIndex(15)
//                        newRemaining = second
//                        get15(first)
//                    }

                    remainingIds.size >= 10 -> {
                        val (first, second) = remainingIds.splitAtIndex(10)
                        newRemaining = second
                        get10(first)
                    }

                    remainingIds.size >= 5 -> {
                        val (first, second) = remainingIds.splitAtIndex(5)
                        newRemaining = second
                        get5(first)
                    }

                    remainingIds.size == 4 -> get4(remainingIds)
                    remainingIds.size == 3 -> get3(remainingIds)
                    remainingIds.size == 2 -> get2(remainingIds)
                    else -> listOfNotNull(get1(remainingIds.single()))
                }
                remainingIds = newRemaining
            }
            results
        }
    }

    private suspend fun getMedia1(id: Int) =
        apolloClient.query(MediaByIdQuery(Optional.Present(id)))
            .execute().data?.media

    private suspend fun getMedias2(@Size(min = 2, max = 2) ids: List<Int>) = apolloClient.query(
        MediaByIds2Query(
            id0 = Optional.Present(ids[0]),
            id1 = Optional.Present(ids[1]),
        )
    ).execute().data?.let {
        listOfNotNull(
            it.media0,
            it.media1,
        )
    }.orEmpty()

    private suspend fun getMedias3(@Size(min = 3, max = 3) ids: List<Int>) = apolloClient.query(
        MediaByIds3Query(
            id0 = Optional.Present(ids[0]),
            id1 = Optional.Present(ids[1]),
            id2 = Optional.Present(ids[2]),
        )
    ).execute().data?.let {
        listOfNotNull(
            it.media0,
            it.media1,
            it.media2,
        )
    }.orEmpty()

    private suspend fun getMedias4(@Size(min = 4, max = 4) ids: List<Int>) = apolloClient.query(
        MediaByIds4Query(
            id0 = Optional.Present(ids[0]),
            id1 = Optional.Present(ids[1]),
            id2 = Optional.Present(ids[2]),
            id3 = Optional.Present(ids[3]),
        )
    ).execute().data?.let {
        listOfNotNull(
            it.media0,
            it.media1,
            it.media2,
            it.media3,
        )
    }.orEmpty()

    private suspend fun getMedias5(@Size(min = 5, max = 5) ids: List<Int>) = apolloClient.query(
        MediaByIds5Query(
            id0 = Optional.Present(ids[0]),
            id1 = Optional.Present(ids[1]),
            id2 = Optional.Present(ids[2]),
            id3 = Optional.Present(ids[3]),
            id4 = Optional.Present(ids[4]),
        )
    ).execute().data?.let {
        listOfNotNull(
            it.media0,
            it.media1,
            it.media2,
            it.media3,
            it.media4,
        )
    }.orEmpty()

    private suspend fun getMedias10(@Size(min = 10, max = 10) ids: List<Int>) = apolloClient.query(
        MediaByIds10Query(
            id0 = Optional.Present(ids[0]),
            id1 = Optional.Present(ids[1]),
            id2 = Optional.Present(ids[2]),
            id3 = Optional.Present(ids[3]),
            id4 = Optional.Present(ids[4]),
            id5 = Optional.Present(ids[5]),
            id6 = Optional.Present(ids[6]),
            id7 = Optional.Present(ids[7]),
            id8 = Optional.Present(ids[8]),
            id9 = Optional.Present(ids[9]),
        )
    ).execute().data?.let {
        listOfNotNull(
            it.media0,
            it.media1,
            it.media2,
            it.media3,
            it.media4,
            it.media5,
            it.media6,
            it.media7,
            it.media8,
            it.media9,
        )
    }.orEmpty()

    private suspend fun getMedias15(@Size(min = 15, max = 15) ids: List<Int>) = apolloClient.query(
        MediaByIds15Query(
            id0 = Optional.Present(ids[0]),
            id1 = Optional.Present(ids[1]),
            id2 = Optional.Present(ids[2]),
            id3 = Optional.Present(ids[3]),
            id4 = Optional.Present(ids[4]),
            id5 = Optional.Present(ids[5]),
            id6 = Optional.Present(ids[6]),
            id7 = Optional.Present(ids[7]),
            id8 = Optional.Present(ids[8]),
            id9 = Optional.Present(ids[9]),
            id10 = Optional.Present(ids[10]),
            id11 = Optional.Present(ids[11]),
            id12 = Optional.Present(ids[12]),
            id13 = Optional.Present(ids[13]),
            id14 = Optional.Present(ids[14]),
        )
    ).execute().data?.let {
        listOfNotNull(
            it.media0,
            it.media1,
            it.media2,
            it.media3,
            it.media4,
            it.media5,
            it.media6,
            it.media7,
            it.media8,
            it.media9,
            it.media10,
            it.media11,
            it.media12,
            it.media13,
            it.media14,
        )
    }.orEmpty()

    private suspend fun getCharacter1(id: Int) =
        apolloClient.query(CharacterByIdQuery(Optional.Present(id)))
            .execute().data?.character

    private suspend fun getCharacters2(@Size(min = 2, max = 2) ids: List<Int>) = apolloClient.query(
        CharacterByIds2Query(
            id0 = Optional.Present(ids[0]),
            id1 = Optional.Present(ids[1]),
        )
    ).execute().data?.let {
        listOfNotNull(
            it.character0,
            it.character1,
        )
    }.orEmpty()

    private suspend fun getCharacters3(@Size(min = 3, max = 3) ids: List<Int>) = apolloClient.query(
        CharacterByIds3Query(
            id0 = Optional.Present(ids[0]),
            id1 = Optional.Present(ids[1]),
            id2 = Optional.Present(ids[2]),
        )
    ).execute().data?.let {
        listOfNotNull(
            it.character0,
            it.character1,
            it.character2,
        )
    }.orEmpty()

    private suspend fun getCharacters4(@Size(min = 4, max = 4) ids: List<Int>) = apolloClient.query(
        CharacterByIds4Query(
            id0 = Optional.Present(ids[0]),
            id1 = Optional.Present(ids[1]),
            id2 = Optional.Present(ids[2]),
            id3 = Optional.Present(ids[3]),
        )
    ).execute().data?.let {
        listOfNotNull(
            it.character0,
            it.character1,
            it.character2,
            it.character3,
        )
    }.orEmpty()

    private suspend fun getCharacters5(@Size(min = 5, max = 5) ids: List<Int>) = apolloClient.query(
        CharacterByIds5Query(
            id0 = Optional.Present(ids[0]),
            id1 = Optional.Present(ids[1]),
            id2 = Optional.Present(ids[2]),
            id3 = Optional.Present(ids[3]),
            id4 = Optional.Present(ids[4]),
        )
    ).execute().data?.let {
        listOfNotNull(
            it.character0,
            it.character1,
            it.character2,
            it.character3,
            it.character4,
        )
    }.orEmpty()

    private suspend fun getCharacters10(@Size(min = 10, max = 10) ids: List<Int>) =
        apolloClient.query(
            CharacterByIds10Query(
                id0 = Optional.Present(ids[0]),
                id1 = Optional.Present(ids[1]),
                id2 = Optional.Present(ids[2]),
                id3 = Optional.Present(ids[3]),
                id4 = Optional.Present(ids[4]),
                id5 = Optional.Present(ids[5]),
                id6 = Optional.Present(ids[6]),
                id7 = Optional.Present(ids[7]),
                id8 = Optional.Present(ids[8]),
                id9 = Optional.Present(ids[9]),
            )
        ).execute().data?.let {
            listOfNotNull(
                it.character0,
                it.character1,
                it.character2,
                it.character3,
                it.character4,
                it.character5,
                it.character6,
                it.character7,
                it.character8,
                it.character9,
            )
        }.orEmpty()

    private suspend fun getCharacters15(@Size(min = 15, max = 15) ids: List<Int>) =
        apolloClient.query(
            CharacterByIds15Query(
                id0 = Optional.Present(ids[0]),
                id1 = Optional.Present(ids[1]),
                id2 = Optional.Present(ids[2]),
                id3 = Optional.Present(ids[3]),
                id4 = Optional.Present(ids[4]),
                id5 = Optional.Present(ids[5]),
                id6 = Optional.Present(ids[6]),
                id7 = Optional.Present(ids[7]),
                id8 = Optional.Present(ids[8]),
                id9 = Optional.Present(ids[9]),
                id10 = Optional.Present(ids[10]),
                id11 = Optional.Present(ids[11]),
                id12 = Optional.Present(ids[12]),
                id13 = Optional.Present(ids[13]),
                id14 = Optional.Present(ids[14]),
            )
        ).execute().data?.let {
            listOfNotNull(
                it.character0,
                it.character1,
                it.character2,
                it.character3,
                it.character4,
                it.character5,
                it.character6,
                it.character7,
                it.character8,
                it.character9,
                it.character10,
                it.character11,
                it.character12,
                it.character13,
                it.character14,
            )
        }.orEmpty()

    fun searchSeries(query: String) =
        apolloClient.query(
            MediaSearchQuery(
                search = Optional.Present(query),
                page = Optional.Present(0),
                perPage = Optional.Present(10),
            )
        ).toFlow()

    fun searchCharacters(query: String) =
        apolloClient.query(
            CharactersSearchQuery(
                search = Optional.Present(query),
                page = Optional.Present(0),
                perPage = Optional.Present(10),
                mediaPage = Optional.Present(0),
                mediaPerPage = Optional.Present(1),
            )
        ).toFlow()

    fun charactersByMedia(mediaId: String) =
        apolloClient.query(
            MediaWithCharactersQuery(
                mediaId = Optional.Present(mediaId.toInt()),
                page = Optional.Present(0),
                perPage = Optional.Present(25),
            )
        ).toFlow()
            .mapNotNull { it.data?.media?.characters?.nodes?.filterNotNull() }
            .mapNotNull { getCharacters(it.map { it.id }) }
}