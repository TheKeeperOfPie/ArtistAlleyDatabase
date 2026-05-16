package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistTableAutocomplete
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class FormArtistTableAutocomplete(
    applicationScope: ApplicationScope,
    dispatchers: CustomDispatchers,
    artistEntryDao: ArtistEntryDao,
) : ArtistTableAutocomplete(
    applicationScope = applicationScope,
    dispatchers = dispatchers,
    loadTables = { artistEntryDao.getTables(it) },
)
