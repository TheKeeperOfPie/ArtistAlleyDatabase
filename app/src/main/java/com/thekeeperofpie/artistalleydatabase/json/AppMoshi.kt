package com.thekeeperofpie.artistalleydatabase.json

import android.util.Log
import com.squareup.moshi.Moshi
import com.thekeeperofpie.artistalleydatabase.anilist.CharacterColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.MediaColumnEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.utils.Converters
import com.thekeeperofpie.artistalleydatabase.utils.Either
import java.math.BigDecimal
import java.util.Date

class AppMoshi {

    companion object {
        private const val TAG = "AppMoshi"
    }

    private val moshi by lazy {
        Moshi.Builder()
            .add(Date::class.java, Converters.DateConverter)
            .add(BigDecimal::class.java, Converters.BigDecimalConverter)
            .build()
    }

    val artEntryAdapter by lazy { moshi.adapter(ArtEntry::class.java)!! }
    val aniListSeriesEntryAdapter = moshi.adapter(MediaColumnEntry::class.java).lenient()!!
    val aniListCharacterEntryAdapter = moshi.adapter(CharacterColumnEntry::class.java).lenient()!!

    fun parseSeriesColumn(value: String?): Either<String, MediaColumnEntry> {
        if (value?.contains("{") == true) {
            try {
                aniListSeriesEntryAdapter.fromJson(value)
                    ?.let { return Either.Right(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing series column: $value")
            }
        }

        return Either.Left(value ?: "")
    }

    fun parseCharacterColumn(value: String?): Either<String, CharacterColumnEntry> {
        if (value?.contains("{") == true) {
            try {
                aniListCharacterEntryAdapter.fromJson(value)
                    ?.let { return Either.Right(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing character column: $value")
            }
        }

        return Either.Left(value ?: "")
    }

    fun toJson(entry: CharacterColumnEntry) = aniListCharacterEntryAdapter.toJson(entry)!!

    fun toJson(entry: MediaColumnEntry) = aniListSeriesEntryAdapter.toJson(entry)!!
}