package com.thekeeperofpie.artistalleydatabase.animethemes

import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.android_utils.addLoggingInterceptors
import com.thekeeperofpie.artistalleydatabase.animethemes.models.Anime
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeResponse
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

class AnimeThemesApi(
    val appJson: AppJson,
    networkSettings: NetworkSettings,
) {
    companion object {
        private const val TAG = "AnimeThemesApi"

        private const val API_BASE_URL = "https://api.animethemes.moe"
        private val ANIME_INCLUDES = listOf(
            "animethemes.song",
            "animethemes.song.artists",
            "animethemes.animethemeentries.videos",
            "animethemes.animethemeentries.videos.audio",
        ).joinToString(",")


        private fun apiPath(aniListMediaId: String) = "$API_BASE_URL/anime" +
                "?filter[has]=resources" +
                "&include=$ANIME_INCLUDES" +
                "&filter[site]=AniList" +
                "&filter[external_id]=$aniListMediaId"
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addLoggingInterceptors(TAG, networkSettings)
        .build()

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getAnime(aniListMediaId: String): Anime? {
        val response = suspendCancellableCoroutine {
            val call = okHttpClient.newCall(
                Request.Builder()
                    .get()
                    .url(apiPath(aniListMediaId))
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

        return appJson.json.decodeFromStream<AnimeResponse>(response.body.byteStream())
            .anime.firstOrNull()
    }
}
