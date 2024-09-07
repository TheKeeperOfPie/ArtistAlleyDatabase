package com.thekeeperofpie.artistalleydatabase

import com.thekeeperofpie.anichive.BuildConfig
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationOverrideProvider
import com.thekeeperofpie.artistalleydatabase.secrets.Secrets
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject

class AppFeatureOverrideProvider : FeatureOverrideProvider {
    // TODO: Use BuildVariant
    @Suppress("KotlinConstantConditions")
    override val isReleaseBuild = BuildConfig.BUILD_TYPE == "release"
    override val enableAppMediaPlayerCache = true
}

@SingletonScope
@Inject
class AppMonetizationOverrideProvider(
    scope: ApplicationScope,
    aniListApi: AuthedAniListApi,
) : MonetizationOverrideProvider {
    // To allow store review testing, hardcode user ID of known test account to unlock all features
    override val overrideUnlock =
        aniListApi.authedUser.map { it?.id == Secrets.aniListTestAccountUserId }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = false,
            )
}
