package com.thekeeperofpie.artistalleydatabase.monetization.unity

import android.app.Activity
import android.app.Application
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.UnityAds
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

class UnityMonetizationProvider(
    private val application: Application,
    private val settings: MonetizationSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : MonetizationProvider {

    companion object {
        private const val FORCE_TEST_MODE = false
    }

    override var error by mutableStateOf<Pair<Int, Throwable?>?>(null)

    private val testMode = FORCE_TEST_MODE || !featureOverrideProvider.isReleaseBuild
    private var adsInitialized = false
    private var adsReady by mutableStateOf(false)
    private lateinit var activity: Activity

    override fun initialize(activity: Activity) {
        if (::activity.isInitialized) return
        this.activity = activity
        if (settings.adsEnabled.value) {
            initializeAds()
        }
    }

    @Composable
    override fun BannerAdView() {
        if (adsReady) {
            AndroidView(
                factory = {
                    BannerView(
                        activity,
                        BuildConfig.unityBannerAdUnitId,
                        UnityBannerSize(320, 50),
                    ).apply {
                        load()
                    }
                },
                onRelease = BannerView::destroy,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            )
        }
    }

    override fun requestEnableAds() {
        settings.adsEnabled.value = true
        initializeAds()
    }

    override fun revokeAds() {
        settings.adsEnabled.value = false
    }

    private fun initializeAds() {
        if (adsInitialized) return
        adsInitialized = true
        UnityAds.initialize(
            application,
            BuildConfig.unityGameId,
            testMode,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    adsReady = true
                }

                override fun onInitializationFailed(
                    initError: UnityAds.UnityAdsInitializationError?,
                    message: String?,
                ) {
                    val errorTextRes =
                        if (initError == UnityAds.UnityAdsInitializationError.AD_BLOCKER_DETECTED) {
                            R.string.unity_error_initializing_ads_ad_blocker_detected
                        } else {
                            R.string.unity_error_initializing_ads_generic
                        }
                    error = errorTextRes to Exception(message)
                }
            },
        )
    }
}
