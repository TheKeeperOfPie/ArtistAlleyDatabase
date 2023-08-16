package com.thekeeperofpie.artistalleydatabase.anime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AnimeRootViewModel @Inject constructor(
    private val oAuthStore: AniListOAuthStore,
    private val monetizationController: MonetizationController,
    settings: AnimeSettings,
) : ViewModel() {

    val lastCrash = settings.lastCrash
    val lastCrashShown = settings.lastCrashShown
    val persistedSelectedScreen = settings.rootNavDestination.value

    val authToken = oAuthStore.authToken

    val unlocked = monetizationController.unlocked

    fun unlocked() = monetizationController.adsEnabled.value
            || monetizationController.subscribed.value

    fun onSubmitAuthToken(token: String) {
        viewModelScope.launch(CustomDispatchers.IO) {
            oAuthStore.storeAuthTokenResult(token)
        }
    }
}
