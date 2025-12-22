@file:OptIn(ExperimentalUuidApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.async.coroutines.awaitCreate
import com.thekeeperofpie.artistalleydatabase.alley.data.ColumnAdapters
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormNonce
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormPublicKey
import com.thekeeperofpie.artistalleydatabase.alley.functions.form.AlleyFormDatabase
import kotlin.uuid.ExperimentalUuidApi

internal object Databases {

    suspend fun editDatabase(
        context: EventContext,
        tryCreate: Boolean = false,
    ): AlleySqlDatabase {
        val sqlDriver = WorkerSqlDriver(database = context.env.ARTIST_ALLEY_DB)
        val database = AlleySqlDatabase(
            driver = sqlDriver,
            artistEntryAnimeExpo2026Adapter = ColumnAdapters.artistEntryAnimeExpo2026Adapter,
            artistEntryAnimeExpo2026HistoryAdapter = ArtistEntryAnimeExpo2026History.Adapter(
                statusAdapter = ColumnAdapters.artistStatusAdapter,
                linksAdapter = ColumnAdapters.listStringAdapter,
                storeLinksAdapter = ColumnAdapters.listStringAdapter,
                catalogLinksAdapter = ColumnAdapters.listStringAdapter,
                seriesInferredAdapter = ColumnAdapters.listStringAdapter,
                seriesConfirmedAdapter = ColumnAdapters.listStringAdapter,
                merchInferredAdapter = ColumnAdapters.listStringAdapter,
                merchConfirmedAdapter = ColumnAdapters.listStringAdapter,
                commissionsAdapter = ColumnAdapters.listStringAdapter,
                imagesAdapter = ColumnAdapters.listCatalogImageAdapter,
                lastEditTimeAdapter = ColumnAdapters.instantAdapter,
            ),
            seriesEntryAdapter = ColumnAdapters.seriesEntryAdapter,
        )
        if (tryCreate) {
            // TODO: Manually initiate create to avoid running on every invocation
            AlleySqlDatabase.Schema.awaitCreate(sqlDriver)
        }
        return database
    }

    suspend fun formDatabase(context: EventContext): AlleyFormDatabase {
        val sqlDriver = WorkerSqlDriver(database = context.env.ARTIST_ALLEY_FORM_DB)
        val database = AlleyFormDatabase(
            driver = sqlDriver,
            artistFormNonceAdapter = ArtistFormNonce.Adapter(
                artistIdAdapter = ColumnAdapters.uuidAdapter,
                nonceAdapter = ColumnAdapters.uuidAdapter,
                timestampAdapter = ColumnAdapters.instantAdapter,
            ),
            artistFormPublicKeyAdapter = ArtistFormPublicKey.Adapter(
                artistIdAdapter = ColumnAdapters.uuidAdapter,
            )
        )
        // TODO: Manually initiate create to avoid running on every invocation
        AlleySqlDatabase.Schema.awaitCreate(sqlDriver)
        return database
    }
}
