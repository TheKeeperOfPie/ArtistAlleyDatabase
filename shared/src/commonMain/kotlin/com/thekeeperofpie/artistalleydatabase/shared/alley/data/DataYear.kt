package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.datetime.FixedOffsetTimeZone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
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
    val year: Int,
    val artistTableName: String,
    val stampRallyTableName: String?,
    val folderName: String,
    val dates: ClosedRange<LocalDate>,
    val timeZone: TimeZone,
) {
    @SerialName("AX2023")
    ANIME_EXPO_2023(
        serializedName = "AX2023",
        convention = Convention.ANIME_EXPO,
        year = 2023,
        artistTableName = "artistEntry2023",
        stampRallyTableName = "stampRallyEntry2023",
        folderName = "2023",
        dates = LocalDate(year = 2023, Month.JULY, 1)..LocalDate(year = 2023, Month.JULY, 4),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -7)),
    ),

    @SerialName("AX2024")
    ANIME_EXPO_2024(
        serializedName = "AX2024",
        convention = Convention.ANIME_EXPO,
        year = 2024,
        artistTableName = "artistEntry2024",
        stampRallyTableName = "stampRallyEntry2024",
        folderName = "2024",
        dates = LocalDate(year = 2024, Month.JULY, 4)..LocalDate(year = 2024, Month.JULY, 7),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -7)),
    ),

    @SerialName("AX2025")
    ANIME_EXPO_2025(
        serializedName = "AX2025",
        convention = Convention.ANIME_EXPO,
        year = 2025,
        artistTableName = "artistEntry2025",
        stampRallyTableName = "stampRallyEntry2025",
        folderName = "2025",
        dates = LocalDate(year = 2025, Month.JULY, 3)..LocalDate(year = 2025, Month.JULY, 6),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -7)),
    ),

    @SerialName("AX2026")
    ANIME_EXPO_2026(
        serializedName = "AX2026",
        convention = Convention.ANIME_EXPO,
        year = 2026,
        artistTableName = "artistEntryAnimeExpo2026",
        stampRallyTableName = "stampRallyEntryAnimeExpo2026",
        folderName = "animeExpo2026",
        dates = LocalDate(year = 2026, Month.JULY, 2)..LocalDate(year = 2026, Month.JULY, 5),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -7)),
    ),

    @SerialName("ANYC2024")
    ANIME_NYC_2024(
        serializedName = "ANYC2024",
        convention = Convention.ANIME_NYC,
        year = 2024,
        artistTableName = "artistEntryAnimeNyc2024",
        stampRallyTableName = null,
        folderName = "animeNyc2024",
        dates = LocalDate(year = 2024, Month.AUGUST, 23)..LocalDate(year = 2024, Month.AUGUST, 25),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -4)),
    ),

    @SerialName("ANYC2025")
    ANIME_NYC_2025(
        serializedName = "ANYC2025",
        convention = Convention.ANIME_NYC,
        year = 2025,
        artistTableName = "artistEntryAnimeNyc2025",
        stampRallyTableName = null,
        folderName = "animeNyc2025",
        dates = LocalDate(year = 2025, Month.AUGUST, 21)..LocalDate(year = 2025, Month.AUGUST, 24),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -4)),
    ),
    ;

    val stampRallyTableNameOrThrow: String
        get() = this.stampRallyTableName
            ?: throw IllegalStateException("$serializedName shouldn't have rallies")

    enum class Convention {
        ANIME_EXPO,
        ANIME_NYC,
    }

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
