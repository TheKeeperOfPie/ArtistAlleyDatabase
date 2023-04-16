package com.thekeeperofpie.artistalleydatabase.anime.media.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.MediaDetailsQuery
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeMediaDetailsViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
) : ViewModel() {

    lateinit var mediaId: String

    val loading = MutableStateFlow(false)
    val media = MutableStateFlow<MediaDetailsQuery.Data.Media?>(null)
    var errorResource = MutableStateFlow<Pair<Int, Exception?>?>(null)

    fun initialize(mediaId: String) {
        if (::mediaId.isInitialized) return
        this.mediaId = mediaId

        viewModelScope.launch(CustomDispatchers.IO) {
            loading.value = true
            try {
                media.value = aniListApi.mediaDetails(mediaId).data?.media
            } catch (e: Exception) {
                errorResource.value = R.string.anime_media_error_loading_details to e
            } finally {
                loading.value = false
            }
        }
    }
}