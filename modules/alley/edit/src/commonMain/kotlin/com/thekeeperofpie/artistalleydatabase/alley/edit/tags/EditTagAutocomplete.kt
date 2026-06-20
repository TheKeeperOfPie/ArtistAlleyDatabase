package com.thekeeperofpie.artistalleydatabase.alley.edit.tags

import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
open class EditTagAutocomplete(
    applicationScope: ApplicationScope,
    dispatchers: CustomDispatchers,
    database: AlleyEditDatabase,
) : TagAutocomplete(
    applicationScope = applicationScope,
    dispatchers = dispatchers,
    loadSeries = database::loadSeries,
    loadMerch = database::loadMerch,
)
