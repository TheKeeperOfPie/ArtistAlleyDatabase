package com.thekeeperofpie.artistalleydatabase.anime

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
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

    fun onClickAuth(context: Context) = oAuthStore.launchAuthRequest(context)

    fun onSubmitAuthToken(token: String) {
        viewModelScope.launch(CustomDispatchers.IO) {
            oAuthStore.storeAuthTokenResult(token)
        }
    }
}
