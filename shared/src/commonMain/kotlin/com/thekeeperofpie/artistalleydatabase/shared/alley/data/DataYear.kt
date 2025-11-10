package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = DataYearSerializer::class)
enum class DataYear(
    val serializedName: String,
    val convention: Convention,
    val artistTableName: String,
    val stampRallyTableName: String?,
    val folderName: String,
    val dates: Dates,
) {
    @SerialName("AX2023")
    ANIME_EXPO_2023(
        serializedName = "AX2023",
        convention = Convention.ANIME_EXPO,
        artistTableName = "artistEntry2023",
        stampRallyTableName = "stampRallyEntry2023",
        folderName = "2023",
        dates = Dates(
            year = 2023,
            month = 7,
            startDay = 1,
            endDay = 4,
            timeZoneOffsetHours = -7,
        ),
    ),

    @SerialName("AX2024")
    ANIME_EXPO_2024(
        serializedName = "AX2024",
        convention = Convention.ANIME_EXPO,
        artistTableName = "artistEntry2024",
        stampRallyTableName = "stampRallyEntry2024",
        folderName = "2024",
        dates = Dates(
            year = 2024,
            month = 7,
            startDay = 4,
            endDay = 7,
            timeZoneOffsetHours = -7,
        ),
    ),

    @SerialName("AX2025")
    ANIME_EXPO_2025(
        serializedName = "AX2025",
        convention = Convention.ANIME_EXPO,
        artistTableName = "artistEntry2025",
        stampRallyTableName = "stampRallyEntry2025",
        folderName = "2025",
        dates = Dates(
            year = 2025,
            month = 7,
            startDay = 3,
            endDay = 6,
            timeZoneOffsetHours = -7,
        ),
    ),

    @SerialName("AX2026")
    ANIME_EXPO_2026(
        serializedName = "AX2026",
        convention = Convention.ANIME_EXPO,
        artistTableName = "artistEntryAnimeExpo2026",
        stampRallyTableName = "stampRallyEntryAnimeExpo2026",
        folderName = "animeExpo2026",
        dates = Dates(
            year = 2026,
            month = 7,
            startDay = 2,
            endDay = 5,
            timeZoneOffsetHours = -7,
        ),
    ),

    @SerialName("ANYC2024")
    ANIME_NYC_2024(
        serializedName = "ANYC2024",
        convention = Convention.ANIME_NYC,
        artistTableName = "artistEntryAnimeNyc2024",
        stampRallyTableName = null,
        folderName = "animeNyc2024",
        dates = Dates(
            year = 2024,
            month = 8,
            startDay = 23,
            endDay = 25,
            timeZoneOffsetHours = -4,
        ),
    ),

    @SerialName("ANYC2025")
    ANIME_NYC_2025(
        serializedName = "ANYC2025",
        convention = Convention.ANIME_NYC,
        artistTableName = "artistEntryAnimeNyc2025",
        stampRallyTableName = null,
        folderName = "animeNyc2025",
        dates = Dates(
            year = 2025,
            month = 8,
            startDay = 21,
            endDay = 24,
            timeZoneOffsetHours = -4,
        ),
    ),
    ;

    val stampRallyTableNameOrThrow: String
        get() = this.stampRallyTableName
            ?: throw IllegalStateException("$serializedName shouldn't have rallies")

    enum class Convention {
        ANIME_EXPO,
        ANIME_NYC,
    }

    // :modules:alley-functions's build fails to import js-joda correctly, so instead of using
    // kotlinx-datetime, this manually declares a data class for the convention date range
    data class Dates(
        val year: Int,
        val month: Int,
        val startDay: Int,
        val endDay: Int,
        val timeZoneOffsetHours: Int,
    )

    companion object {
        val LATEST = ANIME_EXPO_2026

        fun deserialize(value: String) = when (value) {
            "YEAR_2023" -> DataYear.ANIME_EXPO_2023
            "YEAR_2024" -> DataYear.ANIME_EXPO_2024
            "YEAR_2025" -> DataYear.ANIME_EXPO_2025
            else -> DataYear.entries.find { it.serializedName == value }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
class DataYearSerializer : KSerializer<DataYear> {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear",
        kind = PrimitiveKind.STRING,
    )

    override fun serialize(
        encoder: Encoder,
        value: DataYear,
    ) {
        encoder.encodeString(value.serializedName)
    }

    override fun deserialize(decoder: Decoder): DataYear {
        val value = decoder.decodeString()
        return DataYear.deserialize(value)
            ?: throw SerializationException("$value is not a valid enum ${descriptor.serialName}}")
    }

}
