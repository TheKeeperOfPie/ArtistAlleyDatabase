package com.thekeeperofpie.artistalleydatabase.alley.backend.data

import com.thekeeperofpie.artistalleydatabase.alley.data.ColumnAdapters

object BackendColumnAdapters {
    val artistHistoryEntryAdapter = ArtistHistoryEntry.Adapter(
        dataYearAdapter = ColumnAdapters.dataYearAdapter,
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
        profileImageAdapter = ColumnAdapters.databaseImageAdapter,
        lastEditTimeAdapter = ColumnAdapters.instantAdapter,
        formTimestampAdapter = ColumnAdapters.instantAdapter,
        remoteTimestampAdapter = ColumnAdapters.instantAdapter,
    )

    val artistRemoteDataAdapter = ArtistRemoteData.Adapter(
        confirmedIdAdapter = ColumnAdapters.uuidAdapter,
        dataYearAdapter = ColumnAdapters.dataYearAdapter,
        linksAdapter = ColumnAdapters.listStringAdapter,
        timestampAdapter = ColumnAdapters.instantAdapter,
    )

    val artistRemoteDataHistoryAdapter = ArtistRemoteDataHistory.Adapter(
        confirmedIdAdapter = ColumnAdapters.uuidAdapter,
        dataYearAdapter = ColumnAdapters.dataYearAdapter,
        linksAdapter = ColumnAdapters.listStringAdapter,
        timestampAdapter = ColumnAdapters.instantAdapter,
    )

    val stampRallyHistoryEntryAdapter = StampRallyHistoryEntry.Adapter(
        dataYearAdapter = ColumnAdapters.dataYearAdapter,
        tablesAdapter = ColumnAdapters.listStringAdapter,
        startTablesAdapter = ColumnAdapters.setStringAdapter,
        endTablesAdapter = ColumnAdapters.setStringAdapter,
        linksAdapter = ColumnAdapters.listStringAdapter,
        tableMinAdapter = ColumnAdapters.tableMinAdapter,
        prizeMerchAdapter = ColumnAdapters.listStringAdapter,
        seriesAdapter = ColumnAdapters.listStringAdapter,
        merchAdapter = ColumnAdapters.listStringAdapter,
        imagesAdapter = ColumnAdapters.listDatabaseImageAdapter,
        lastEditTimeAdapter = ColumnAdapters.instantAdapter,
        formTimestampAdapter = ColumnAdapters.instantAdapter,
    )

    val stampRallyQueueEntryAdapter = StampRallyQueueEntry.Adapter(
        dataYearAdapter = ColumnAdapters.dataYearAdapter,
        boothsAdapter = ColumnAdapters.setStringAdapter,
    )
}
