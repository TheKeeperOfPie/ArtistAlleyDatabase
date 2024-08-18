package com.thekeeperofpie.artistalleydatabase.monetization.unity.secrets

import com.thekeeperofpie.artistalleydatabase.unity.secrets.BuildConfig

/**
 * The secrets plugin doesn't seem to support KMP, so manually delegate to the BuildConfig here.
 */
object UnitySecrets {
    const val unityBannerAdUnitId = BuildConfig.unityBannerAdUnitId
    const val unityGameId = BuildConfig.unityGameId
}
