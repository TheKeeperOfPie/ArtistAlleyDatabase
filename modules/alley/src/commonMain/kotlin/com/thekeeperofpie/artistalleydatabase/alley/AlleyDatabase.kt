package com.thekeeperofpie.artistalleydatabase.alley

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
            databaseState.value = AlleySqlDatabase(driver)
        }
    }

    internal val artistEntryDao = ArtistEntryDao(driver, database, json)
    internal val stampRallyEntryDao = StampRallyEntryDao(driver, database, json)
    internal val tagEntryDao = TagEntryDao(driver, database)

    fun artistEntryDao() = artistEntryDao
    fun stampRallyEntryDao() = stampRallyEntryDao
    fun tagEntryDao() = tagEntryDao
}
