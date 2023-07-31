@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.home

import android.os.SystemClock
import androidx.compose.runtime.mutableStateMapOf
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
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.entryId
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.liked
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.subscribed
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeActivityViewModel
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeHomeViewModel @Inject constructor(
    val newsController: AnimeNewsController,
    private val aniListApi: AuthedAniListApi,
    ignoreList: AnimeMediaIgnoreList,
    activityStatusController: ActivityStatusController,
    settings: AnimeSettings,
) : ViewModel() {

    val viewer = aniListApi.authedUser
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
                    }.flow.cachedIn(viewModelScope)
                }
                .flatMapLatest { pagingData ->
                    combine(
                        ignoreList.updates,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { ignoredIds, showLessImportantTags, showSpoilerTags ->
                        pagingData.map {
                            AnimeActivityViewModel.ActivityEntry(
                                it.entryId,
                                it,
                                it.liked,
                                it.subscribed,
                                (it as? UserSocialActivityQuery.Data.Page.ListActivityActivity)
                                    ?.media?.let {
                                        AnimeActivityViewModel.ActivityEntry.MediaEntry(
                                            media = it,
                                            ignored = ignoredIds.contains(it.id),
                                            showLessImportantTags = showLessImportantTags,
                                            showSpoilerTags = showSpoilerTags,
                                        )
                                    },
                            )
                        }
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
                .cachedIn(viewModelScope)
                .collectLatest(activity::emit)
        }
    }

    fun refresh() {
        newsController.refresh()
        refreshUptimeMillis.value = SystemClock.uptimeMillis()
    }
}
