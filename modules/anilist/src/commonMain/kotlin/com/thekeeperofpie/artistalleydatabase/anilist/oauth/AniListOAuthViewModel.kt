package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anilist.generated.resources.Res
import artistalleydatabase.modules.anilist.generated.resources.aniList_oAuth_error
import artistalleydatabase.modules.anilist.generated.resources.aniList_oAuth_error_bad_url
import com.eygraber.uri.Uri
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

@AssistedInject
class AniListOAuthViewModel(
    private val oAuthStore: PlatformOAuthStore,
    @Assisted text: String?,
) : ViewModel() {

    companion object {
        private const val DEBUG_ERROR = false
    }

    var state by mutableStateOf<State>(State.Loading)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val state = if (text == null) {
                State.Error(Res.string.aniList_oAuth_error)
            } else if (text.startsWith("com.thekeeperofpie.anichive")) {
                if (!text.contains("access_token")) {
                    State.Error(Res.string.aniList_oAuth_error_bad_url)
                } else {
                    parseAndStoreFragment(text)
                }
            } else if (text.startsWith("http")) {
                if (!text.contains("/api/v2/oauth/pin")) {
                    State.Error(Res.string.aniList_oAuth_error_bad_url)
                } else {
                    parseAndStoreFragment(text)
                }
            } else {
                oAuthStore.storeAuthTokenResult(text)
                State.Done
            }

            launch(Dispatchers.Main) {
                this@AniListOAuthViewModel.state = state
            }
        }
    }

    private suspend fun parseAndStoreFragment(text: String) = try {
        // AniList token page exposes access_token inside the URL fragment
        val token = Uri.parse("text://host?${Uri.parse(text).encodedFragment}")
            .getQueryParameter("access_token")
        if (token == null || DEBUG_ERROR) {
            State.Error(Res.string.aniList_oAuth_error_bad_url)
        } else {
            oAuthStore.storeAuthTokenResult(token)
            State.Done
        }
    } catch (exception: Exception) {
        State.Error(Res.string.aniList_oAuth_error, exception)
    }

    sealed interface State {
        data object Loading : State
        data object Done : State
        data class Error(val textRes: StringResource, val exception: Exception? = null) : State
    }

    @AssistedFactory
    interface Factory {
        fun create(text: String?): AniListOAuthViewModel
    }
}
