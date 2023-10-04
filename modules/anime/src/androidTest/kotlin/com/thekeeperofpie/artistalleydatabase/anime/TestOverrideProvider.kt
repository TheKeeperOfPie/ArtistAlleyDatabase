package com.thekeeperofpie.artistalleydatabase.anime

import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationOverrideProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TestOverrideProvider : FeatureOverrideProvider, MonetizationOverrideProvider {

    override val isReleaseBuild: Boolean = false
    override val enableAppMediaPlayerCache = false
    override val overrideUnlock: Flow<Boolean> = MutableStateFlow(false)
}
