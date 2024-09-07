package com.thekeeperofpie.artistalleydatabase.animethemes

import com.thekeeperofpie.artistalleydatabase.animethemes.models.Anime
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeResponse
import com.thekeeperofpie.artistalleydatabase.animethemes.models.ArtistResponse
import com.thekeeperofpie.artistalleydatabase.animethemes.models.ArtistWithAniList
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readBuffer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalSerializationApi::class)
@SingletonScope
@Inject
class AnimeThemesApi(
    private val json: Json,
    private val httpClient: HttpClient,
) {
    companion object {
        private const val API_BASE_URL = "https://api.animethemes.moe"
        private val ANIME_INCLUDES = listOf(
            "animethemes.song.artists.images",
            "animethemes.animethemeentries.videos.audio",
        ).joinToString(",")
    }

    suspend fun getAnime(aniListMediaId: String): Anime? {
        val response = executeGet(
            "$API_BASE_URL/anime" +
                    "?filter[has]=resources" +
                    "&include=$ANIME_INCLUDES" +
                    "&filter[site]=AniList" +
                    "&filter[external_id]=$aniListMediaId"
        )

        return json.decodeFromSource<AnimeResponse>(response)
            .anime.firstOrNull()
    }

    // TODO: Use this API to actually associate by ID rather than string matching
    suspend fun artistWithAniList(artistSlug: String): ArtistWithAniList {
        val response = executeGet(
            "$API_BASE_URL/artist/$artistSlug" +
                    "?filter[site]=AniList" +
                    "&include=resources"
        )

        return json.decodeFromSource<ArtistResponse>(response).artist
    }

    private suspend fun executeGet(url: String) = httpClient.get(url).bodyAsChannel().readBuffer()
}
