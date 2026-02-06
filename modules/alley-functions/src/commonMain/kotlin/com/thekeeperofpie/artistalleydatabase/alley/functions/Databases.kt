@file:OptIn(ExperimentalUuidApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.async.coroutines.awaitCreate
import com.thekeeperofpie.artistalleydatabase.alley.data.ColumnAdapters
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormEntry
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormEntryHistory
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormNonce
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormPublicKey
import com.thekeeperofpie.artistalleydatabase.alley.form.StampRallyFormEntry
import com.thekeeperofpie.artistalleydatabase.alley.functions.form.AlleyFormDatabase
import kotlin.uuid.ExperimentalUuidApi

internal object Databases {

    private fun editSqlDriver(context: EventContext) =
        WorkerSqlDriver(database = context.env.ARTIST_ALLEY_DB)

    private fun formSqlDriver(context: EventContext) =
        WorkerSqlDriver(database = context.env.ARTIST_ALLEY_FORM_DB)

    suspend fun create(context: EventContext) {
        AlleySqlDatabase.Schema.awaitCreate(editSqlDriver(context))
        AlleyFormDatabase.Schema.awaitCreate(formSqlDriver(context))
    }

    fun editDatabase(context: EventContext) = AlleySqlDatabase(
        driver = editSqlDriver(context),
        artistEntryAnimeExpo2026Adapter = ColumnAdapters.artistEntryAnimeExpo2026Adapter,
        artistEntryAnimeExpo2026HistoryAdapter = ArtistEntryAnimeExpo2026History.Adapter(
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
            imagesAdapter = ColumnAdapters.listCatalogImageAdapter,
            lastEditTimeAdapter = ColumnAdapters.instantAdapter,
            formTimestampAdapter = ColumnAdapters.instantAdapter,
        ),
        seriesEntryAdapter = ColumnAdapters.seriesEntryAdapter,
        stampRallyEntryAnimeExpo2026Adapter = ColumnAdapters.stampRallyEntryAnimeExpo2026Adapter,
        stampRallyEntryAnimeExpo2026HistoryAdapter = StampRallyEntryAnimeExpo2026History.Adapter(
            tablesAdapter = ColumnAdapters.listStringAdapter,
            linksAdapter = ColumnAdapters.listStringAdapter,
            tableMinAdapter = ColumnAdapters.tableMinAdapter,
            seriesAdapter = ColumnAdapters.listStringAdapter,
            merchAdapter = ColumnAdapters.listStringAdapter,
            imagesAdapter = ColumnAdapters.listCatalogImageAdapter,
            lastEditTimeAdapter = ColumnAdapters.instantAdapter,
            formTimestampAdapter = ColumnAdapters.instantAdapter,
        ),
    )

    fun formDatabase(context: EventContext) = AlleyFormDatabase(
        driver = formSqlDriver(context),
        artistFormEntryAdapter = ArtistFormEntry.Adapter(
            artistIdAdapter = ColumnAdapters.uuidAdapter,
            dataYearAdapter = ColumnAdapters.dataYearAdapter,
            beforeSocialLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeStoreLinksAdapter = ColumnAdapters.listStringAdapter,
            beforePortfolioLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeCatalogLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeCommissionsAdapter = ColumnAdapters.listStringAdapter,
            beforeSeriesInferredAdapter = ColumnAdapters.listStringAdapter,
            beforeSeriesConfirmedAdapter = ColumnAdapters.listStringAdapter,
            beforeMerchInferredAdapter = ColumnAdapters.listStringAdapter,
            beforeMerchConfirmedAdapter = ColumnAdapters.listStringAdapter,
            beforeImagesAdapter = ColumnAdapters.listCatalogImageAdapter,
            afterSocialLinksAdapter = ColumnAdapters.listStringAdapter,
            afterStoreLinksAdapter = ColumnAdapters.listStringAdapter,
            afterPortfolioLinksAdapter = ColumnAdapters.listStringAdapter,
            afterCatalogLinksAdapter = ColumnAdapters.listStringAdapter,
            afterCommissionsAdapter = ColumnAdapters.listStringAdapter,
            afterSeriesInferredAdapter = ColumnAdapters.listStringAdapter,
            afterSeriesConfirmedAdapter = ColumnAdapters.listStringAdapter,
            afterMerchInferredAdapter = ColumnAdapters.listStringAdapter,
            afterMerchConfirmedAdapter = ColumnAdapters.listStringAdapter,
            afterImagesAdapter = ColumnAdapters.listCatalogImageAdapter,
            timestampAdapter = ColumnAdapters.instantAdapter,
        ),
        artistFormEntryHistoryAdapter = ArtistFormEntryHistory.Adapter(
            artistIdAdapter = ColumnAdapters.uuidAdapter,
            dataYearAdapter = ColumnAdapters.dataYearAdapter,
            beforeSocialLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeStoreLinksAdapter = ColumnAdapters.listStringAdapter,
            beforePortfolioLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeCatalogLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeCommissionsAdapter = ColumnAdapters.listStringAdapter,
            beforeSeriesInferredAdapter = ColumnAdapters.listStringAdapter,
            beforeSeriesConfirmedAdapter = ColumnAdapters.listStringAdapter,
            beforeMerchInferredAdapter = ColumnAdapters.listStringAdapter,
            beforeMerchConfirmedAdapter = ColumnAdapters.listStringAdapter,
            beforeImagesAdapter = ColumnAdapters.listCatalogImageAdapter,
            afterSocialLinksAdapter = ColumnAdapters.listStringAdapter,
            afterStoreLinksAdapter = ColumnAdapters.listStringAdapter,
            afterPortfolioLinksAdapter = ColumnAdapters.listStringAdapter,
            afterCatalogLinksAdapter = ColumnAdapters.listStringAdapter,
            afterCommissionsAdapter = ColumnAdapters.listStringAdapter,
            afterSeriesInferredAdapter = ColumnAdapters.listStringAdapter,
            afterSeriesConfirmedAdapter = ColumnAdapters.listStringAdapter,
            afterMerchInferredAdapter = ColumnAdapters.listStringAdapter,
            afterMerchConfirmedAdapter = ColumnAdapters.listStringAdapter,
            afterImagesAdapter = ColumnAdapters.listCatalogImageAdapter,
            timestampAdapter = ColumnAdapters.instantAdapter,
        ),
        artistFormNonceAdapter = ArtistFormNonce.Adapter(
            artistIdAdapter = ColumnAdapters.uuidAdapter,
            nonceAdapter = ColumnAdapters.uuidAdapter,
            timestampAdapter = ColumnAdapters.instantAdapter,
        ),
        artistFormPublicKeyAdapter = ArtistFormPublicKey.Adapter(
            artistIdAdapter = ColumnAdapters.uuidAdapter,
        ),
        stampRallyFormEntryAdapter = StampRallyFormEntry.Adapter(
            dataYearAdapter = ColumnAdapters.dataYearAdapter,
            artistIdAdapter = ColumnAdapters.uuidAdapter,
            beforeTablesAdapter = ColumnAdapters.listStringAdapter,
            beforeLinksAdapter = ColumnAdapters.listStringAdapter,
            beforeTableMinAdapter = ColumnAdapters.tableMinAdapter,
            beforeSeriesAdapter = ColumnAdapters.listStringAdapter,
            beforeMerchAdapter = ColumnAdapters.listStringAdapter,
            beforeImagesAdapter = ColumnAdapters.listCatalogImageAdapter,
            afterTablesAdapter = ColumnAdapters.listStringAdapter,
            afterLinksAdapter = ColumnAdapters.listStringAdapter,
            afterTableMinAdapter = ColumnAdapters.tableMinAdapter,
            afterSeriesAdapter = ColumnAdapters.listStringAdapter,
            afterMerchAdapter = ColumnAdapters.listStringAdapter,
            afterImagesAdapter = ColumnAdapters.listCatalogImageAdapter,
            timestampAdapter = ColumnAdapters.instantAdapter,
        ),
    )
}
