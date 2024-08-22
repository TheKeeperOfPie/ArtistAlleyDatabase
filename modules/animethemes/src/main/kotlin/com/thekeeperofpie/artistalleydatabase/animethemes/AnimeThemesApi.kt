package com.thekeeperofpie.artistalleydatabase.animethemes

import com.thekeeperofpie.artistalleydatabase.animethemes.models.Anime
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeResponse
import com.thekeeperofpie.artistalleydatabase.animethemes.models.ArtistResponse
import com.thekeeperofpie.artistalleydatabase.animethemes.models.ArtistWithAniList
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalSerializationApi::class)
class AnimeThemesApi(
    val appJson: AppJson,
    private val okHttpClient: OkHttpClient,
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

        return appJson.json.decodeFromStream<AnimeResponse>(response.body.byteStream())
            .anime.firstOrNull()
    }

    // TODO: Use this API to actually associate by ID rather than string matching
    suspend fun artistWithAniList(artistSlug: String): ArtistWithAniList {
        val response = executeGet(
            "$API_BASE_URL/artist/$artistSlug" +
                    "?filter[site]=AniList" +
                    "&include=resources"
        )

        return appJson.json.decodeFromStream<ArtistResponse>(response.body.byteStream()).artist
    }

    private suspend fun executeGet(url: String) = suspendCancellableCoroutine {
        val call = okHttpClient.newCall(
            Request.Builder()
                .get()
                .url(url)
                .build()
        )
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                it.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                it.resume(response)
            }
        })

        it.invokeOnCancellation {
            call.cancel()
        }
    }
}
