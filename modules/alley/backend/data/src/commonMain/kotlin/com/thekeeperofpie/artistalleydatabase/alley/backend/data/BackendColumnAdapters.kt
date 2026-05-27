package com.thekeeperofpie.artistalleydatabase.alley.backend.data

import com.thekeeperofpie.artistalleydatabase.alley.data.ColumnAdapters

object BackendColumnAdapters {
    val artistEntryAnimeExpo2026HistoryAdapter = ArtistEntryAnimeExpo2026History.Adapter(
        statusAdapter = ColumnAdapters.artistStatusAdapter,
        socialLinksAdapter = ColumnAdapters.listStringAdapter,
        storeLinksAdapter = ColumnAdapters.listStringAdapter,
        portfolioLinksAdapter = ColumnAdapters.listStringAdapter,
        catalogLinksAdapter = ColumnAdapters.listStringAdapter,
        seriesInferredAdapter = ColumnAdapters.listStringAdapter,
        seriesConfirmedAdapter = ColumnAdapters.listStringAdapter,
        merchInferredAdapter = ColumnAdapters.listStringAdapter,
        merchConfirmedAdapter = ColumnAdapters.listStringAdapter,
        commissionsAdapter = ColumnAdapters.listStringAdapter,
        imagesAdapter = ColumnAdapters.listDatabaseImageAdapter,
        lastEditTimeAdapter = ColumnAdapters.instantAdapter,
        formTimestampAdapter = ColumnAdapters.instantAdapter,
        remoteTimestampAdapter = ColumnAdapters.instantAdapter,
    )

    val artistRemoteDataAnimeExpo2026Adapter = ArtistRemoteDataAnimeExpo2026.Adapter(
        confirmedIdAdapter = ColumnAdapters.uuidAdapter,
        linksAdapter = ColumnAdapters.listStringAdapter,
        timestampAdapter = ColumnAdapters.instantAdapter,
    )

    val artistRemoteDataAnimeExpo2026HistoryAdapter = ArtistRemoteDataAnimeExpo2026History.Adapter(
        confirmedIdAdapter = ColumnAdapters.uuidAdapter,
        linksAdapter = ColumnAdapters.listStringAdapter,
        timestampAdapter = ColumnAdapters.instantAdapter,
    )

    val stampRallyEntryAnimeExpo2026HistoryAdapter = StampRallyEntryAnimeExpo2026History.Adapter(
        tablesAdapter = ColumnAdapters.listStringAdapter,
        startTablesAdapter = ColumnAdapters.setStringAdapter,
        endTablesAdapter = ColumnAdapters.setStringAdapter,
        linksAdapter = ColumnAdapters.listStringAdapter,
        tableMinAdapter = ColumnAdapters.tableMinAdapter,
        seriesAdapter = ColumnAdapters.listStringAdapter,
        merchAdapter = ColumnAdapters.listStringAdapter,
        imagesAdapter = ColumnAdapters.listDatabaseImageAdapter,
        lastEditTimeAdapter = ColumnAdapters.instantAdapter,
        formTimestampAdapter = ColumnAdapters.instantAdapter,
    )
}
