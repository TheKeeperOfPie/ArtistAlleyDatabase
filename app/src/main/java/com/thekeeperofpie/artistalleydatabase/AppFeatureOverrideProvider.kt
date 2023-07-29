package com.thekeeperofpie.artistalleydatabase

import com.thekeeperofpie.anichive.BuildConfig
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider

class AppFeatureOverrideProvider : FeatureOverrideProvider {
    @Suppress("KotlinConstantConditions")
    override val isReleaseBuild = BuildConfig.BUILD_TYPE == "release"
}
