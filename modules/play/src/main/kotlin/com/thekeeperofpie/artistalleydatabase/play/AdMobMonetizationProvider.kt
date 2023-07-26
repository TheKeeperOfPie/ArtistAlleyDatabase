package com.thekeeperofpie.artistalleydatabase.play

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import java.util.concurrent.atomic.AtomicBoolean

class AdMobMonetizationProvider(
    private val application: Application,
    private val settings: MonetizationSettings,
) : MonetizationProvider {

    companion object {
        private const val TAG = "AdMobMonetizationProvider"
    }

    override var error by mutableStateOf<Pair<Int, Throwable?>?>(null)

    private lateinit var consentInfo: ConsentInformation
    private var readyForConsentForm = false
    private var adRequest by mutableStateOf<AdRequest?>(null)
    private val adsInitialized = AtomicBoolean(false)
    private var immediatelyShowForm = false

    private lateinit var activity: Activity

    override fun initialize(activity: Activity) {
        if (::activity.isInitialized) return
        this.activity = activity
        if (!settings.adsEnabled.value) return
        requestConsentInfo()
    }

    @Composable
    override fun BannerAdView() {
        val request = adRequest
        AndroidView(
            factory = {
                AdView(it).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = BuildConfig.adMobBannerAdUnitId
                    adListener = object : AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            super.onAdFailedToLoad(loadAdError)
                            // TODO: Disable features on frequent failure to load
                        }
                    }
                }
            },
            onRelease = { it.destroy() },
            update = {
                val existingRequest = it.getTag(R.id.ad_request_key)
                if (request != null && existingRequest != request) {
                    it.setTag(R.id.ad_request_key, request)
                    it.loadAd(request)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .height(50.dp)
        )
    }

    override fun requestEnableAds() {
        immediatelyShowForm = true
        if (readyForConsentForm) {
            showConsentForm()
        } else if (!::consentInfo.isInitialized) {
            requestConsentInfo()
        } else {
            onConsentError(null)
        }
    }

    override fun onAdsRevoked() {
        settings.adsEnabled.value = false
        if (::consentInfo.isInitialized) {
            consentInfo.reset()
        }
    }

    private fun requestConsentInfo() {
        if (::consentInfo.isInitialized) return
        val consentRequestParams = ConsentRequestParameters.Builder()
            .apply {
                val umpTestDeviceHash = BuildConfig.umpTestDeviceHash
                @Suppress("KotlinConstantConditions", "UselessCallOnNotNull")
                if (!umpTestDeviceHash.isNullOrEmpty() && umpTestDeviceHash != "null") {
                    setConsentDebugSettings(
                        ConsentDebugSettings.Builder(activity)
                            .setDebugGeography(
                                ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
                            )
                            .addTestDeviceHashedId(umpTestDeviceHash)
                            .setForceTesting(true)
                            .build()
                    )
                }
            }
            .build()
        consentInfo = UserMessagingPlatform.getConsentInformation(activity)
        consentInfo.requestConsentInfoUpdate(
            activity,
            consentRequestParams,
            ::onConsentInfoSuccess,
            ::onConsentError
        )
        if (consentInfo.canRequestAds()) {
            initializeAds()
        }
    }

    private fun onConsentInfoSuccess() {
        readyForConsentForm = true
        if (immediatelyShowForm) {
            immediatelyShowForm = false
            showConsentForm()
        } else if (consentInfo.canRequestAds()) {
            initializeAds()
        }
    }

    private fun showConsentForm() {
        UserMessagingPlatform.loadConsentForm(
            activity,
            {
                it.show(activity) {
                    if (consentInfo.canRequestAds()) {
                        initializeAds()
                    }
                }
            },
            ::onConsentError
        )
    }

    private fun onConsentError(requestConsentError: FormError?) {
        Log.d(
            TAG,
            "onConsentError() called with: requestConsentError =" +
                    "${requestConsentError?.errorCode} ${requestConsentError?.message}"
        )
        error = R.string.play_error_loading_consent_form to null
    }

    private fun initializeAds() {
        settings.adsEnabled.value = true
        if (adsInitialized.getAndSet(true)) return
//        MobileAds.initialize(application) {
//            adRequest = AdRequest.Builder().build()
//        }
    }
}
