package com.thekeeperofpie.artistalleydatabase.monetization

import kotlinx.coroutines.flow.StateFlow

interface MonetizationOverrideProvider {
    val overrideUnlock: StateFlow<Boolean>
}
