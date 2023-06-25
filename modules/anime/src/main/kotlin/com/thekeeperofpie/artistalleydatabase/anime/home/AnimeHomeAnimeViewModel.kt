package com.thekeeperofpie.artistalleydatabase.anime.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.HomeAnimeQuery
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AnimeHomeAnimeViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi
) : ViewModel() {

    var entry by mutableStateOf<Entry?>(null)
    var errorResource by mutableStateOf<Pair<Int, Throwable?>?>(null)

    private val refreshUptimeMillis = MutableStateFlow(-1)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            refreshUptimeMillis
                .map {aniListApi.homeAnime() }
                .map { Entry(it) }
                .map(Result.Companion::success)
                .catch { emit(Result.failure(it)) }
                .collectLatest {
                    withContext(CustomDispatchers.Main) {
                        if (it.isFailure) {
                            errorResource = R.string.anime_home_error_loading_anime to it.exceptionOrNull()
                        } else {
                            entry = it.getOrNull()
                        }
                    }
                }
        }
    }

    data class Entry(
        private val data: HomeAnimeQuery.Data
    ) {
        val trending = data.trending?.media?.filterNotNull().orEmpty()
        val popularThisSeason = data.popularThisSeason?.media?.filterNotNull().orEmpty()
        val popularLastSeason = data.popularLastSeason?.media?.filterNotNull().orEmpty()
        val popularNextSeason = data.popularNextSeason?.media?.filterNotNull().orEmpty()
        val popular = data.popular?.media?.filterNotNull().orEmpty()
        val top = data.top?.media?.filterNotNull().orEmpty()
    }
}
