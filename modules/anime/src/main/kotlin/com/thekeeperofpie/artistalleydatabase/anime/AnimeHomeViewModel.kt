package com.thekeeperofpie.artistalleydatabase.anime

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AnimeHomeViewModel @Inject constructor(
    private val oAuthStore: AniListOAuthStore,
) : ViewModel() {

    var needAuth by mutableStateOf(false)

    init {
        viewModelScope.launch(Dispatchers.Main) {
            oAuthStore.hasAuth.collectLatest {
                needAuth = !it
            }
        }
    }

    fun onClickAuth(activity: Activity) {
        oAuthStore.launchAuthRequest(activity)
    }

    // TODO: Remove this
    fun authTokenDebugPreview() = oAuthStore.authToken?.take(20)
}