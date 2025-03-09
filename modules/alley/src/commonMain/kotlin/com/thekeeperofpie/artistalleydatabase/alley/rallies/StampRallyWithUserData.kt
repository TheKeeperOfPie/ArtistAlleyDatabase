package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry

data class StampRallyWithUserData(
    val stampRally: StampRallyEntry,
    val userEntry: StampRallyUserEntry,
)
