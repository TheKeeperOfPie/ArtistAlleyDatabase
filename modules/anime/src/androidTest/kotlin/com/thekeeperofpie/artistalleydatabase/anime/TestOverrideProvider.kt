package com.thekeeperofpie.artistalleydatabase.anime

import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.FeatureOverrideProvider
import kotlinx.coroutines.flow.MutableStateFlow

class TestOverrideProvider : FeatureOverrideProvider, MonetizationOverrideProvider {

    override val isReleaseBuild: Boolean = false
    override val enableAppMediaPlayerCache = false
    override val overrideUnlock = MutableStateFlow(false)
}
