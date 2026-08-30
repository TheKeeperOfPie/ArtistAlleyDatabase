package com.thekeeperofpie.artistalleydatabase.alley.discord

import com.thekeeperofpie.artistalleydatabase.alley.backend.data.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.form.data.AlleyFormDatabase
import com.thekeeperofpie.artistalleydatabase.cloudflare.WorkerSqlDriver

internal object Databases {

    fun editSqlDriver(env: Env) =
        WorkerSqlDriver(database = env.ARTIST_ALLEY_DB)

    fun formSqlDriver(env: Env) =
        WorkerSqlDriver(database = env.ARTIST_ALLEY_FORM_DB)

    fun backendDatabase(env: Env) = AlleySqlDatabase(
        driver = editSqlDriver(env),
    )

    fun formDatabase(env: Env) = AlleyFormDatabase(
        driver = formSqlDriver(env),
    )
}
