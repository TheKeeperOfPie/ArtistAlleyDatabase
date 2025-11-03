package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.user.AlleyUserDatabase
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SingleIn(AppScope::class)
@Inject
class ArtistAlleyDatabase(
    applicationScope: ApplicationScope,
    settings: ArtistAlleySettings,
    userDatabase: AlleyUserDatabase,
) {
    private val databaseState = MutableStateFlow<AlleySqlDatabase?>(null)
    private val database = suspend { databaseState.filterNotNull().first() }
    private val driverState = MutableStateFlow<SqlDriver?>(null)
    private val driver = suspend { driverState.filterNotNull().first() }

    init {
        applicationScope.launch(PlatformDispatchers.IO) {
            driverState.value = userDatabase.createDriver()
            databaseState.value = DaoUtils.createAlleySqlDatabase(driver())
            // TODO: Retain only valid IDs
        }
    }

    internal val artistEntryDao = ArtistEntryDao(driver, database, settings)
    internal val stampRallyEntryDao = StampRallyEntryDao(driver, database)
    internal val imageEntryDao = ImageEntryDao(database)
    internal val importExportDao = ImportExportDao(driver, database)
    internal val userNotesDao = UserNotesDao(database)
    internal val merchEntryDao = MerchEntryDao(driver, database)
    internal val seriesEntryDao = SeriesEntryDao(driver, database)
    internal val tagEntryDao = TagEntryDao(database)
    internal val userEntryDao = UserEntryDao(driver, database, settings)
}
