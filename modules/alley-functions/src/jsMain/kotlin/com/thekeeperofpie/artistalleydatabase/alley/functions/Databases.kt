package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.functions.form.AlleyFormDatabase
import com.thekeeperofpie.artistalleydatabase.alley.functions.user.AlleyUserDatabase

@Suppress("UnusedReceiverParameter")
internal suspend fun BackendDatabases.create(context: EventContext) {
    AlleySqlDatabase.Schema.awaitCreate(DatabaseDrivers.editSqlDriver(context))
    AlleyFormDatabase.Schema.awaitCreate(DatabaseDrivers.formSqlDriver(context))
    AlleyUserDatabase.Schema.awaitCreate(DatabaseDrivers.userSqlDriver(context))
}

internal fun BackendDatabases.editDatabase(context: EventContext) =
    editDatabase(DatabaseDrivers.editSqlDriver(context))

internal fun BackendDatabases.formDatabase(context: EventContext) =
    formDatabase(DatabaseDrivers.formSqlDriver(context))

internal fun BackendDatabases.userDatabase(
    context: EventContext,
    driver: SqlDriver = DatabaseDrivers.userSqlDriver(context),
) = userDatabase(driver)
