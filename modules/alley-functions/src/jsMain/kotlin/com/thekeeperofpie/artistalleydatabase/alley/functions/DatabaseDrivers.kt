package com.thekeeperofpie.artistalleydatabase.alley.functions

import com.thekeeperofpie.artistalleydatabase.cloudflare.WorkerSqlDriver

internal object DatabaseDrivers {

    fun editSqlDriver(context: EventContext) =
        WorkerSqlDriver(database = context.env.ARTIST_ALLEY_DB)

    fun formSqlDriver(context: EventContext) =
        WorkerSqlDriver(database = context.env.ARTIST_ALLEY_FORM_DB)

    fun userSqlDriver(context: EventContext) =
        WorkerSqlDriver(database = context.env.ARTIST_ALLEY_USER_DB)
}
