package com.thekeeperofpie.artistalleydatabase.anilist

import com.anilist.CharactersByIdsQuery
import com.anilist.CharactersSearchQuery
import com.anilist.MediaSearchQuery
import com.anilist.MediaWithCharactersQuery
import com.anilist.SimpleMediaByIdsQuery
import com.anilist.fragment.AniListCharacter
import com.anilist.fragment.AniListMedia
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.chunked
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import okhttp3.OkHttpClient
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class AniListApi(
    application: ScopedApplication,
    networkSettings: NetworkSettings,
    okHttpClient: OkHttpClient,
) {
    companion object {
        private const val TAG = "AniListApi"
    }

    private val apolloClient = ApolloClient.Builder()
        .serverUrl(AniListUtils.GRAPHQL_API_URL)
        .httpEngine(DefaultHttpEngine(okHttpClient))
        .addLoggingInterceptors(TAG, networkSettings)
        .build()

    private data class Request<T>(
        val id: String,
        val result: CompletableDeferred<T?> = CompletableDeferred(),
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

    suspend fun getMedias(ids: List<Int>) = ids.chunked(25).flatMap {
        apolloClient.query(SimpleMediaByIdsQuery(ids = Optional.present(it))).execute()
            .dataOrThrow().page?.media?.filterNotNull().orEmpty().filter { it.isAdult == false }
    }

    suspend fun getCharacters(ids: List<Int>) = ids.chunked(25).flatMap {
        apolloClient.query(CharactersByIdsQuery(ids = Optional.present(it))).execute()
            .dataOrThrow().page?.characters?.filterNotNull().orEmpty().map {
                it.copy(media = it.media?.copy(nodes = it.media.nodes?.filter { it?.isAdult == false }))
            }
    }

    fun searchSeries(query: String) =
        apolloClient.query(
            MediaSearchQuery(
                search = Optional.Present(query),
                page = Optional.Present(0),
                perPage = Optional.Present(10),
            )
        ).toFlow().map {
            val data = it.data
            data?.copy(page = data.page.copy(media = data.page.media.filter { it?.isAdult == false }))
        }

    fun searchCharacters(query: String) =
        apolloClient.query(
            CharactersSearchQuery(
                search = Optional.Present(query),
                page = Optional.Present(0),
                perPage = Optional.Present(10),
                mediaPage = Optional.Present(0),
                mediaPerPage = Optional.Present(1),
            )
        ).toFlow().map {
            val data = it.data
            data?.copy(page = data.page.copy(characters = data.page.characters.map {
                it?.copy(media = it.media?.copy(it.media.nodes?.filter { it?.isAdult == false }))
            }))
        }

    fun charactersByMedia(mediaId: String) =
        apolloClient.query(
            MediaWithCharactersQuery(
                mediaId = Optional.Present(mediaId.toInt()),
                page = Optional.Present(0),
                perPage = Optional.Present(25),
            )
        ).toFlow()
            .mapNotNull { it.takeIf { it.data?.media?.isAdult == false } }
            .mapNotNull { it.data?.media?.characters?.nodes?.filterNotNull() }
            .mapNotNull { getCharacters(it.map { it.id }) }
}
