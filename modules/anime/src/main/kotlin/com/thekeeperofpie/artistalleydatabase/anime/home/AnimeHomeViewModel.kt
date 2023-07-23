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
import androidx.paging.map
import com.anilist.UserSocialActivityQuery
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.entryId
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.liked
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.subscribed
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeActivityViewModel
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AnimeHomeViewModel @Inject constructor(
    val newsController: AnimeNewsController,
    private val aniListApi: AuthedAniListApi,
    activityStatusController: ActivityStatusController,
) : ViewModel() {

    val viewer = aniListApi.authedUser
    var loading by mutableStateOf(false)
        private set
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()
    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    private val refreshUptimeMillis = MutableStateFlow(-1L)
    val activity = MutableStateFlow(PagingData.empty<AnimeActivityViewModel.ActivityEntry>())

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(refreshUptimeMillis, aniListApi.authedUser, ::Pair)
                .flatMapLatest { (_, viewer) ->
                    Pager(config = PagingConfig(3)) {
                        AniListPagingSource {
                            val result = aniListApi.userSocialActivity(
                                isFollowing = viewer != null,
                                page = it,
                                perPage = 3,
                                userIdNot = viewer?.id,
                            )
                            result.page?.pageInfo to
                                    result.page?.activities?.filterNotNull().orEmpty()
                        }
                    }.flow
                }
                .mapLatest {
                    it.map {
                        AnimeActivityViewModel.ActivityEntry(
                            it.entryId,
                            it,
                            it.liked,
                            it.subscribed,
                            (it as? UserSocialActivityQuery.Data.Page.ListActivityActivity)
                                ?.media?.let(AnimeActivityViewModel.ActivityEntry::MediaEntry),
                        )
                    }
                }
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    activityStatusController.allChanges()
                        .mapLatest { updates ->
                            pagingData.map {
                                val liked = updates[it.activityId.valueId]?.liked ?: it.liked
                                val subscribed =
                                    updates[it.activityId.valueId]?.subscribed ?: it.subscribed
                                it.transformIf(liked != it.liked || subscribed != it.subscribed) {
                                    it.copy(liked = liked, subscribed = subscribed)
                                }
                            }
                        }
                }
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
