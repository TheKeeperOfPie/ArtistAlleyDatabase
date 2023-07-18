@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.home

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.UserSocialActivityQuery
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AnimeHomeViewModel @Inject constructor(
    val newsController: AnimeNewsController,
    private val aniListApi: AuthedAniListApi,
) : ViewModel() {

    val viewer = aniListApi.authedUser
    var loading by mutableStateOf(false)
        private set
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    private val refreshUptimeMillis = MutableStateFlow(-1L)
    val activity = MutableStateFlow(PagingData.empty<UserSocialActivityQuery.Data.Page.Activity>())

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(refreshUptimeMillis, aniListApi.authedUser, ::Pair)
                .flatMapLatest { (_, viewer) ->
                    Pager(config = PagingConfig(0)) {
                        AniListPagingSource {
                            val result = aniListApi.userSocialActivity(
                                isFollowing = viewer != null,
                                page = it,
                                userIdNot = viewer?.id,
                            )
                            result.page?.pageInfo to
                                    result.page?.activities?.filterNotNull().orEmpty()
                        }
                    }.flow
                }
                .cachedIn(viewModelScope)
                .collectLatest(activity::emit)
        }
    }

    fun refresh() {
        loading = true
        newsController.refresh()
        refreshUptimeMillis.value = SystemClock.uptimeMillis()

        // Fake the refresh, since the actual state requires combining too many loading states
        viewModelScope.launch {
            delay(250.milliseconds)
            loading = false
        }
    }
}
