package com.thekeeperofpie.artistalleydatabase.alley.merch

import com.thekeeperofpie.artistalleydatabase.alley.data.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.MerchUserEntry

data class MerchWithUserData(
    val merch: MerchEntry,
    val userEntry: MerchUserEntry,
)
