package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2023
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2024
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyEntry2025
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
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
import kotlinx.serialization.json.Json
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
                    artistNamesAdapter = listStringAdapter,
                    linksAdapter = listStringAdapter,
                    catalogLinksAdapter = listStringAdapter,
                ),
                artistEntry2024Adapter = ArtistEntry2024.Adapter(
                    linksAdapter = listStringAdapter,
                    storeLinksAdapter = listStringAdapter,
                    catalogLinksAdapter = listStringAdapter,
                    seriesInferredAdapter = listStringAdapter,
                    seriesConfirmedAdapter = listStringAdapter,
                    merchInferredAdapter = listStringAdapter,
                    merchConfirmedAdapter = listStringAdapter,
                ),
                artistEntry2025Adapter = ArtistEntry2025.Adapter(
                    linksAdapter = listStringAdapter,
                    storeLinksAdapter = listStringAdapter,
                    catalogLinksAdapter = listStringAdapter,
                    seriesInferredAdapter = listStringAdapter,
                    seriesConfirmedAdapter = listStringAdapter,
                    merchInferredAdapter = listStringAdapter,
                    merchConfirmedAdapter = listStringAdapter,
                    commissionsAdapter = listStringAdapter,
                ),
                stampRallyEntry2023Adapter = StampRallyEntry2023.Adapter(
                    tablesAdapter = listStringAdapter,
                    linksAdapter = listStringAdapter,
                ),
                stampRallyEntry2024Adapter = StampRallyEntry2024.Adapter(
                    tablesAdapter = listStringAdapter,
                    linksAdapter = listStringAdapter,
                ),
                stampRallyEntry2025Adapter = StampRallyEntry2025.Adapter(
                    tablesAdapter = listStringAdapter,
                    linksAdapter = listStringAdapter,
                ),
                artistNotesAdapter = ArtistNotes.Adapter(
                    dataYearAdapter = dataYearAdapter,
                ),
                artistUserEntryAdapter = ArtistUserEntry.Adapter(
                    dataYearAdapter = dataYearAdapter,
                ),
            )

            // TODO: Retain only valid IDs
        }
    }

    internal val artistEntryDao = ArtistEntryDao(driver, database, settings)
    internal val stampRallyEntryDao = StampRallyEntryDao(driver, database)
    internal val notesDao = NotesDao(database)
    internal val tagEntryDao = TagEntryDao(driver, database)
    internal val userEntryDao = UserEntryDao(database, settings)
}

private val listStringAdapter = object : ColumnAdapter<List<String>, String> {
    override fun decode(databaseValue: String) = Json.decodeFromString<List<String>>(databaseValue)
    override fun encode(value: List<String>) = Json.encodeToString(value)
}

private val dataYearAdapter = object : ColumnAdapter<DataYear, String> {
    override fun decode(databaseValue: String) =
        DataYear.entries.first { it.serializedName == databaseValue }

    override fun encode(value: DataYear) = value.serializedName
}
