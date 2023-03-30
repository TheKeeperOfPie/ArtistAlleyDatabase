package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AniListOAuthViewModel @Inject constructor(
    private val oAuthStore: AniListOAuthStore,
) : ViewModel() {

    var loading by mutableStateOf(true)

    private var initialized = false

    fun initialize(text: String) {
        if (initialized) return
        initialized = true
        viewModelScope.launch(CustomDispatchers.IO) {
            var token = text.trim()
            if (text.startsWith("http")) {
                try {
                    // AniList token page exposes access_token inside the URL fragment
                    Uri.parse("text://host?${Uri.parse(text).encodedFragment}")
                        .getQueryParameter("access_token")
                        ?.let { token = it }
                } catch (ignored: Exception) {
                    // Exception ignored to avoid leaks
                }
            }

            // TODO: Error
//            if (token.isNullOrBlank()) {
//            }

            oAuthStore.storeAuthTokenResult(token)
            launch(CustomDispatchers.Main) {
                loading = false
            }
        }
    }
}