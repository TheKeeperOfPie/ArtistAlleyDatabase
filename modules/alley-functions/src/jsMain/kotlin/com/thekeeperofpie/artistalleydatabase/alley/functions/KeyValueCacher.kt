package com.thekeeperofpie.artistalleydatabase.alley.functions

import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallySummary
import kotlinx.coroutines.await
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.json.Json

internal class KeyValueCacher(private val context: EventContext) {

    suspend fun putSeries(series: List<SeriesInfo>): String {
        val serializedValue = Json.encodeToString<List<SeriesInfo>>(series)
        context.env.ARTIST_ALLEY_CACHE_KV.put("series", serializedValue).await()
        return serializedValue
    }

    suspend fun getSeriesJson(): String? = try {
        context.env.ARTIST_ALLEY_CACHE_KV.get("series").await()
    } catch(_: Throwable) {
        currentCoroutineContext().ensureActive()
        null
    }

    suspend fun putFakeArtistData(privateKey: String) {
        val serializedValue = Json.encodeToString(FakeArtistData(privateKey))
        context.env.ARTIST_ALLEY_CACHE_KV.put("fakeArtistData", serializedValue).await()
    }

    suspend fun getFakeArtistData(): FakeArtistData? = try {
        context.env.ARTIST_ALLEY_CACHE_KV.get("fakeArtistData").await()
            ?.let { Json.decodeFromString<FakeArtistData?>(it) }
    } catch(_: Throwable) {
        currentCoroutineContext().ensureActive()
        null
    }

    suspend fun putStampRallies(stampRallies: List<StampRallySummary>): String {
        val serializedValue = Json.encodeToString<List<StampRallySummary>>(stampRallies)
        context.env.ARTIST_ALLEY_CACHE_KV.put("stampRallies", serializedValue).await()
        return serializedValue
    }

    suspend fun getStampRalliesJson(): String? = try {
        context.env.ARTIST_ALLEY_CACHE_KV.get("stampRallies").await()
    } catch(_: Throwable) {
        currentCoroutineContext().ensureActive()
        null
    }

    suspend fun invalidateStampRallies(): Unit = try {
        context.env.ARTIST_ALLEY_CACHE_KV.delete("stampRallies").await()
    } catch(_: Throwable) {
        currentCoroutineContext().ensureActive()
    }
}
