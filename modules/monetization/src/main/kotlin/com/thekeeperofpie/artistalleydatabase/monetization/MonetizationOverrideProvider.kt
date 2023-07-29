package com.thekeeperofpie.artistalleydatabase.monetization

import kotlinx.coroutines.flow.Flow

interface MonetizationOverrideProvider {
    val overrideUnlock: Flow<Boolean>
}
