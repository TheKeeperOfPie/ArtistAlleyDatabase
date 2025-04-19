package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSource
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.user.AlleyUserDatabase
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistNotes
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@SingletonScope
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
            databaseState.value = AlleySqlDatabase(
                driver = driver(),
                artistEntry2023Adapter = ArtistEntry2023.Adapter(
                    artistNamesAdapter = DaoUtils.listStringAdapter,
                    linksAdapter = DaoUtils.listStringAdapter,
                    catalogLinksAdapter = DaoUtils.listStringAdapter,
                ),
                artistEntry2024Adapter = ArtistEntry2024.Adapter(
                    linksAdapter = DaoUtils.listStringAdapter,
                    storeLinksAdapter = DaoUtils.listStringAdapter,
                    catalogLinksAdapter = DaoUtils.listStringAdapter,
                    seriesInferredAdapter = DaoUtils.listStringAdapter,
                    seriesConfirmedAdapter = DaoUtils.listStringAdapter,
                    merchInferredAdapter = DaoUtils.listStringAdapter,
                    merchConfirmedAdapter = DaoUtils.listStringAdapter,
                ),
                artistEntry2025Adapter = ArtistEntry2025.Adapter(
                    linksAdapter = DaoUtils.listStringAdapter,
                    storeLinksAdapter = DaoUtils.listStringAdapter,
                    catalogLinksAdapter = DaoUtils.listStringAdapter,
                    seriesInferredAdapter = DaoUtils.listStringAdapter,
                    seriesConfirmedAdapter = DaoUtils.listStringAdapter,
                    merchInferredAdapter = DaoUtils.listStringAdapter,
                    merchConfirmedAdapter = DaoUtils.listStringAdapter,
                    commissionsAdapter = DaoUtils.listStringAdapter,
                ),
                stampRallyEntry2023Adapter = StampRallyEntry2023.Adapter(
                    tablesAdapter = DaoUtils.listStringAdapter,
                    linksAdapter = DaoUtils.listStringAdapter,
                ),
                stampRallyEntry2024Adapter = StampRallyEntry2024.Adapter(
                    tablesAdapter = DaoUtils.listStringAdapter,
                    linksAdapter = DaoUtils.listStringAdapter,
                ),
                stampRallyEntry2025Adapter = StampRallyEntry2025.Adapter(
                    tablesAdapter = DaoUtils.listStringAdapter,
                    linksAdapter = DaoUtils.listStringAdapter,
                ),
                artistNotesAdapter = ArtistNotes.Adapter(
                    dataYearAdapter = DaoUtils.dataYearAdapter,
                ),
                artistUserEntryAdapter = ArtistUserEntry.Adapter(
                    dataYearAdapter = DaoUtils.dataYearAdapter,
                ),
                seriesEntryAdapter = SeriesEntry.Adapter(
                    sourceAdapter = object : ColumnAdapter<SeriesSource, String> {
                        override fun decode(databaseValue: String) =
                            SeriesSource.entries.find { it.name == databaseValue }
                                ?: SeriesSource.NONE

                        override fun encode(value: SeriesSource) = value.name
                    },
                )
            )

            // TODO: Retain only valid IDs
        }
    }

    internal val artistEntryDao = ArtistEntryDao(driver, database, settings)
    internal val stampRallyEntryDao = StampRallyEntryDao(driver, database)
    internal val imageEntryDao = ImageEntryDao(database)
    internal val importExportDao = ImportExportDao(database)
    internal val userNotesDao = UserNotesDao(database)
    internal val seriesEntryDao = SeriesEntryDao(driver, database)
    internal val tagEntryDao = TagEntryDao(driver, database)
    internal val userEntryDao = UserEntryDao(database, settings)
}
