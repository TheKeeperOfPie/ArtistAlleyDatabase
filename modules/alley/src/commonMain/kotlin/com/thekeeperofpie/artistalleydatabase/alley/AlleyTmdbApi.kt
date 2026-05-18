package com.thekeeperofpie.artistalleydatabase.alley

import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TmdbType
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Inject
@SingleIn(AppScope::class)
class AlleyTmdbApi(private val networkClient: NetworkClient) {

    private fun requestUrl(id: String, type: TmdbType) = when (type) {
        TmdbType.NONE, // Shouldn't ever happen
        TmdbType.TV -> "https://api.themoviedb.org/3/tv/$id/images"
        TmdbType.MOVIE -> "https://api.themoviedb.org/3/movie/$id/images"
    }

    suspend fun getMediaImages(mediaIds: Collection<Pair<String, TmdbType>>): Map<String, String> =
        mediaIds.mapNotNull { (id, type) ->
            try {
                networkClient.httpClient.get(requestUrl(id, type)) {
                    bearerAuth(AlleyUtils.tmdbApiKey)
                    header(HttpHeaders.Accept, "application/json")
                }
                    .body<Response>()
                    .posters
                    ?.firstOrNull()
                    ?.let { "https://image.tmdb.org/t/p/w342${it.filePath}" }
                    ?.let { id to it }
            } catch (_: Throwable) {
                null
            }
        }.toMap()

    @Serializable
    data class Response(
        val posters: List<Poster>? = null,
    ) {
        @Serializable
        data class Poster(
            @SerialName("file_path")
            val filePath: String,
        )
    }
}
