package com.thekeeperofpie.artistalleydatabase.alley.discord

import com.thekeeperofpie.artistalleydatabase.alley.backend.data.ArtistCatalogQueueEntry
import com.thekeeperofpie.artistalleydatabase.alley.backend.data.StampRallyQueueEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.ColumnAdapters
import com.thekeeperofpie.artistalleydatabase.alley.discord.form.AlleyFormDatabase
import com.thekeeperofpie.artistalleydatabase.alley.form.data.ArtistFormPublicKey
import com.thekeeperofpie.artistalleydatabase.cloudflare.WorkerSqlDriver

internal object Databases {

    fun editSqlDriver(env: Env) =
        WorkerSqlDriver(database = env.ARTIST_ALLEY_DB)

    fun formSqlDriver(env: Env) =
        WorkerSqlDriver(database = env.ARTIST_ALLEY_FORM_DB)

    fun backendDatabase(env: Env) = AlleySqlDatabase(
        driver = editSqlDriver(env),
        artistCatalogQueueEntryAdapter = ArtistCatalogQueueEntry.Adapter(
            dataYearAdapter = ColumnAdapters.dataYearAdapter,
        ),
        artistEntryAnimeExpo2026Adapter = ColumnAdapters.artistEntryAnimeExpo2026Adapter,
        stampRallyEntryAnimeExpo2026Adapter = ColumnAdapters.stampRallyEntryAnimeExpo2026Adapter,
        stampRallyQueueEntryAdapter = StampRallyQueueEntry.Adapter(
            dataYearAdapter = ColumnAdapters.dataYearAdapter,
            boothsAdapter = ColumnAdapters.setStringAdapter,
        ),
    )

    fun formDatabase(env: Env) = AlleyFormDatabase(
        driver = formSqlDriver(env),
        artistFormPublicKeyAdapter = ArtistFormPublicKey.Adapter(
            artistIdAdapter = ColumnAdapters.uuidAdapter,
        ),
    )
}
