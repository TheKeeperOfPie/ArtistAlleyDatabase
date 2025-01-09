package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.ColumnAdapter
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.DriverFactory
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class ArtistAlleyDatabase(
    applicationScope: ApplicationScope,
    json: Json,
    driverFactory: DriverFactory,
) {
    private val databaseState = MutableStateFlow<AlleySqlDatabase?>(null)
    private val database = suspend { databaseState.filterNotNull().first() }
    private val driver = driverFactory.createDriver()

    init {
        applicationScope.launch(PlatformDispatchers.IO) {
            driverFactory.applySchema(driver)
            databaseState.value = AlleySqlDatabase(
                driver = driver,
                artistEntryAdapter = ArtistEntry.Adapter(
                    linksAdapter = listStringAdapter,
                    storeLinksAdapter = listStringAdapter,
                    catalogLinksAdapter = listStringAdapter,
                    seriesInferredAdapter = listStringAdapter,
                    seriesConfirmedAdapter = listStringAdapter,
                    merchInferredAdapter = listStringAdapter,
                    merchConfirmedAdapter = listStringAdapter,
                ),
                stampRallyEntryAdapter = StampRallyEntry.Adapter(
                    tablesAdapter = listStringAdapter,
                    linksAdapter = listStringAdapter,
                ),
            )
        }
    }

    internal val artistEntryDao = ArtistEntryDao(driver, database)
    internal val stampRallyEntryDao = StampRallyEntryDao(driver, database, json)
    internal val tagEntryDao = TagEntryDao(driver, database)
}

private val listStringAdapter = object : ColumnAdapter<List<String>, String> {
    override fun decode(databaseValue: String) = Json.decodeFromString<List<String>>(databaseValue)
    override fun encode(value: List<String>) = Json.encodeToString(value)
}
