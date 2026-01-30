package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry

data class StampRallyWithUserData(
    val stampRally: StampRallyDatabaseEntry,
    val userEntry: StampRallyUserEntry,
)
