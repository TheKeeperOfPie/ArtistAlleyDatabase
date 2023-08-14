package com.thekeeperofpie.artistalleydatabase

import com.thekeeperofpie.anichive.BuildConfig
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationOverrideProvider
import kotlinx.coroutines.flow.map

class AppFeatureOverrideProvider : FeatureOverrideProvider {
    @Suppress("KotlinConstantConditions")
    override val isReleaseBuild = BuildConfig.BUILD_TYPE == "release"
}

class AppMonetizationOverrideProvider(
    aniListApi: AuthedAniListApi,
    featureOverrideProvider: FeatureOverrideProvider,
) : MonetizationOverrideProvider {
    // To allow store review testing, hardcode user ID of known test account to unlock all features
    override val overrideUnlock =
        aniListApi.authedUser.map { it?.id == BuildConfig.aniListTestAccountUserId.toInt() }
}
