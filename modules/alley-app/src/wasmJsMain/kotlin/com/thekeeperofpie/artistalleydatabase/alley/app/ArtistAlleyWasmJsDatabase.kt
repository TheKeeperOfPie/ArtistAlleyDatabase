package com.thekeeperofpie.artistalleydatabase.alley.app

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.app.dao.ArtistEntryDaoImpl
import com.thekeeperofpie.artistalleydatabase.alley.app.dao.StampRallyEntryDaoImpl
import com.thekeeperofpie.artistalleydatabase.alley.app.dao.TagEntryDaoImpl
import com.thekeeperofpie.artistalleydatabase.app.ArtistAlleyAppDatabase
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import org.w3c.dom.Worker

private fun createCustomWorker(): Worker =
    js("""new Worker(new URL("@thekeeperofpie/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url))""")

@Inject
class ArtistAlleyWasmJsDatabase(
    applicationScope: ApplicationScope,
    json: Json,
) : ArtistAlleyDatabase {

    private val databaseState = MutableStateFlow<ArtistAlleyAppDatabase?>(null)
    private val database = suspend { databaseState.filterNotNull().first() }
    private val driver = WebWorkerDriver(createCustomWorker())

    init {
        applicationScope.launch(PlatformDispatchers.IO) {
            ArtistAlleyAppDatabase.Schema.awaitCreate(driver)
            databaseState.value = ArtistAlleyAppDatabase(driver)
        }
    }

    private val artistEntryDao = ArtistEntryDaoImpl(driver, database, json)
    private val stampRallyEntryDao = StampRallyEntryDaoImpl(driver, database, json)
    private val tagEntryDao = TagEntryDaoImpl(driver, database)

    override fun artistEntryDao() = artistEntryDao
    override fun stampRallyEntryDao() = stampRallyEntryDao
    override fun tagEntryDao() = tagEntryDao
}