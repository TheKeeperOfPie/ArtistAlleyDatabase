package com.thekeeperofpie.artistalleydatabase.anime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AnimeHomeViewModel @Inject constructor(
    private val oAuthStore: AniListOAuthStore,
) : ViewModel() {

    val needsAuth = oAuthStore.hasAuth.map { !it }

    fun onSubmitAuthToken(token: String) {
        viewModelScope.launch(CustomDispatchers.IO) {
            oAuthStore.storeAuthTokenResult(token)
        }
    }
}
