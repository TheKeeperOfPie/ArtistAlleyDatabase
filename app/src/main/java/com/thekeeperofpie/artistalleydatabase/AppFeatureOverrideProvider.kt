package com.thekeeperofpie.artistalleydatabase

import com.thekeeperofpie.anichive.BuildConfig
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.FeatureOverrideProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AppFeatureOverrideProvider : FeatureOverrideProvider {
    @Suppress("KotlinConstantConditions")
    override val isReleaseBuild = BuildConfig.BUILD_TYPE == "release"
    override val enableAppMediaPlayerCache = true
}

class AppMonetizationOverrideProvider(
    scopedApplication: ScopedApplication,
    aniListApi: AuthedAniListApi,
) : MonetizationOverrideProvider {
    // To allow store review testing, hardcode user ID of known test account to unlock all features
    override val overrideUnlock =
        aniListApi.authedUser.map { it?.id == BuildConfig.aniListTestAccountUserId }
            .stateIn(
                scope = scopedApplication.scope,
                started = SharingStarted.Eagerly,
                initialValue = false,
            )
}
