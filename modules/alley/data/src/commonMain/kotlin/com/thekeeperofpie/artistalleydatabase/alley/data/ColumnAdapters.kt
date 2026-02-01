package com.thekeeperofpie.artistalleydatabase.alley.data

import app.cash.sqldelight.ColumnAdapter
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import kotlinx.serialization.json.Json
import kotlin.time.Instant
import kotlin.uuid.Uuid

object ColumnAdapters {

    val listStringAdapter = object : ColumnAdapter<List<String>, String> {
        override fun decode(databaseValue: String) =
            Json.decodeFromString<List<String>>(databaseValue)

        override fun encode(value: List<String>) = Json.encodeToString(value)
    }

    val listCatalogImageAdapter = object : ColumnAdapter<List<CatalogImage>, String> {
        override fun decode(databaseValue: String) =
            Json.decodeFromString<List<CatalogImage>>(databaseValue)

        override fun encode(value: List<CatalogImage>) = Json.encodeToString(value)
    }

    val artistStatusAdapter = object : ColumnAdapter<ArtistStatus, String> {
        override fun decode(databaseValue: String) =
            ArtistStatus.entries.find { it.name == databaseValue } ?: ArtistStatus.UNKNOWN

        override fun encode(value: ArtistStatus) = value.name
    }

    val instantAdapter = object : ColumnAdapter<Instant, String> {
        override fun decode(databaseValue: String) = try {
            Instant.parse(databaseValue)
        } catch (_: IllegalArgumentException) {
            Instant.DISTANT_PAST
        }

        override fun encode(value: Instant) = value.toString()
    }

    val uuidAdapter = object : ColumnAdapter<Uuid, String> {
        override fun decode(databaseValue: String) = Uuid.parse(databaseValue)

        override fun encode(value: Uuid) = value.toString()
    }

    val dataYearAdapter = object : ColumnAdapter<DataYear, String> {
        override fun decode(databaseValue: String) =
            DataYear.entries.first { it.serializedName == databaseValue }

        override fun encode(value: DataYear) = value.serializedName
    }

    val tableMinAdapter = object : ColumnAdapter<TableMin, Long> {
        override fun decode(databaseValue: Long) = TableMin.parseFromValue(databaseValue.toInt())

        override fun encode(value: TableMin) = value.serializedValue.toLong()
    }

    val artistEntry2023Adapter = ArtistEntry2023.Adapter(
        artistNamesAdapter = listStringAdapter,
        linksAdapter = listStringAdapter,
        catalogLinksAdapter = listStringAdapter,
        imagesAdapter = listCatalogImageAdapter,
    )
    val artistEntry2024Adapter = ArtistEntry2024.Adapter(
        linksAdapter = listStringAdapter,
        storeLinksAdapter = listStringAdapter,
        catalogLinksAdapter = listStringAdapter,
        seriesInferredAdapter = listStringAdapter,
        seriesConfirmedAdapter = listStringAdapter,
        merchInferredAdapter = listStringAdapter,
        merchConfirmedAdapter = listStringAdapter,
        imagesAdapter = listCatalogImageAdapter,
    )
    val artistEntry2025Adapter = ArtistEntry2025.Adapter(
        linksAdapter = listStringAdapter,
        storeLinksAdapter = listStringAdapter,
        catalogLinksAdapter = listStringAdapter,
        seriesInferredAdapter = listStringAdapter,
        seriesConfirmedAdapter = listStringAdapter,
        merchInferredAdapter = listStringAdapter,
        merchConfirmedAdapter = listStringAdapter,
        commissionsAdapter = listStringAdapter,
        imagesAdapter = listCatalogImageAdapter,
    )
    val artistEntryAnimeExpo2026Adapter = ArtistEntryAnimeExpo2026.Adapter(
        statusAdapter = artistStatusAdapter,
        socialLinksAdapter = listStringAdapter,
        storeLinksAdapter = listStringAdapter,
        portfolioLinksAdapter = listStringAdapter,
        catalogLinksAdapter = listStringAdapter,
        seriesInferredAdapter = listStringAdapter,
        seriesConfirmedAdapter = listStringAdapter,
        merchInferredAdapter = listStringAdapter,
        merchConfirmedAdapter = listStringAdapter,
        commissionsAdapter = listStringAdapter,
        imagesAdapter = listCatalogImageAdapter,
        lastEditTimeAdapter = instantAdapter,
    )
    val artistEntryAnimeExpo2026ChangelogAdapter = ArtistEntryAnimeExpo2026Changelog.Adapter(
        artistIdAdapter = uuidAdapter,
        seriesInferredAdapter = listStringAdapter,
        seriesConfirmedAdapter = listStringAdapter,
        merchInferredAdapter = listStringAdapter,
        merchConfirmedAdapter = listStringAdapter,
    )
    val artistEntryAnimeNyc2024Adapter = ArtistEntryAnimeNyc2024.Adapter(
        linksAdapter = listStringAdapter,
        storeLinksAdapter = listStringAdapter,
        catalogLinksAdapter = listStringAdapter,
        seriesInferredAdapter = listStringAdapter,
        seriesConfirmedAdapter = listStringAdapter,
        merchInferredAdapter = listStringAdapter,
        merchConfirmedAdapter = listStringAdapter,
        commissionsAdapter = listStringAdapter,
        imagesAdapter = listCatalogImageAdapter,
    )
    val artistEntryAnimeNyc2025Adapter = ArtistEntryAnimeNyc2025.Adapter(
        linksAdapter = listStringAdapter,
        storeLinksAdapter = listStringAdapter,
        catalogLinksAdapter = listStringAdapter,
        seriesInferredAdapter = listStringAdapter,
        seriesConfirmedAdapter = listStringAdapter,
        merchInferredAdapter = listStringAdapter,
        merchConfirmedAdapter = listStringAdapter,
        commissionsAdapter = listStringAdapter,
        imagesAdapter = listCatalogImageAdapter,
    )
    val stampRallyEntry2023Adapter = StampRallyEntry2023.Adapter(
        tablesAdapter = listStringAdapter,
        linksAdapter = listStringAdapter,
        imagesAdapter = listCatalogImageAdapter,
    )
    val stampRallyEntry2024Adapter = StampRallyEntry2024.Adapter(
        tablesAdapter = listStringAdapter,
        linksAdapter = listStringAdapter,
        imagesAdapter = listCatalogImageAdapter,
        tableMinAdapter = tableMinAdapter,
    )
    val stampRallyEntry2025Adapter = StampRallyEntry2025.Adapter(
        tablesAdapter = listStringAdapter,
        linksAdapter = listStringAdapter,
        seriesAdapter = listStringAdapter,
        imagesAdapter = listCatalogImageAdapter,
        tableMinAdapter = tableMinAdapter,
    )
    val stampRallyEntryAnimeExpo2026Adapter = StampRallyEntryAnimeExpo2026.Adapter(
        tablesAdapter = listStringAdapter,
        linksAdapter = listStringAdapter,
        tableMinAdapter = tableMinAdapter,
        seriesAdapter = listStringAdapter,
        merchAdapter = listStringAdapter,
        imagesAdapter = listCatalogImageAdapter,
        lastEditTimeAdapter = instantAdapter,
    )

    val seriesEntryAdapter = SeriesEntry.Adapter(
        sourceAdapter = object : ColumnAdapter<SeriesSource, String> {
            override fun decode(databaseValue: String) =
                SeriesSource.entries.find { it.name == databaseValue }
                    ?: SeriesSource.NONE

            override fun encode(value: SeriesSource) = value.name
        },
        synonymsAdapter = listStringAdapter,
    )
}
