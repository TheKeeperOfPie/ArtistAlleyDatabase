package com.thekeeperofpie.artistalleydatabase.anime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

@Inject
class AnimeRootViewModel(
    private val oAuthStore: AniListOAuthStore,
    private val monetizationController: MonetizationController,
    settings: AnimeSettings,
) : ViewModel() {

    val lastCrash = settings.lastCrash
    val lastCrashShown = settings.lastCrashShown
    val persistedSelectedScreen = settings.rootNavDestination
    val authToken = oAuthStore.authToken
    val unlocked = monetizationController.unlocked

    fun onSubmitAuthToken(token: String) {
        viewModelScope.launch(CustomDispatchers.IO) {
            oAuthStore.storeAuthTokenResult(token)
        }
    }
}
