package com.thekeeperofpie.artistalleydatabase.alley

import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Inject
@SingleIn(AppScope::class)
class AlleyWikipediaApi(private val networkClient: NetworkClient) {

    private val requestUrl =
        "https://en.wikipedia.org/w/api.php?action=query&prop=pageimages&pilicense=any&pithumbsize=240&origin=*&format=json&pageids="

    suspend fun getMediaImages(mediaIds: Collection<String>) = try {
        networkClient.httpClient
            .get(requestUrl + mediaIds.joinToString(separator = "|"))
            .body<Response>()
            .query?.pages
            ?.filterValues { it.thumbnail?.source != null }
            ?.mapValues { it.value.thumbnail?.source!! }
            .orEmpty()
    } catch (_: Throwable) {
        emptyMap()
    }

    @Serializable
    data class Response(
        val query: Query? = null,
    ) {
        @Serializable
        data class Query(
            val pages: Map<String, Page> = emptyMap(),
        ) {
            @Serializable
            data class Page(
                val thumbnail: Thumbnail? = null,
            ) {
                @Serializable
                data class Thumbnail(
                    val source: String? = null,
                )
            }
        }
    }
}
