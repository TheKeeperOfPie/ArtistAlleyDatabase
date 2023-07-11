package com.thekeeperofpie.artistalleydatabase.anime.activity

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.anilist.UserSocialActivityQuery
import com.anilist.UserSocialActivityQuery.Data.Page.ListActivityActivity
import com.anilist.UserSocialActivityQuery.Data.Page.MessageActivityActivity
import com.anilist.UserSocialActivityQuery.Data.Page.OtherActivity
import com.anilist.UserSocialActivityQuery.Data.Page.TextActivityActivity
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnimeActivityViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
) : ViewModel() {

    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()
    val viewer = aniListApi.authedUser

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    private val globalActivity =
        MutableStateFlow(PagingData.empty<UserSocialActivityQuery.Data.Page.Activity>())
    private var globalActivityJob: Job? = null

    private val followingActivity =
        MutableStateFlow(PagingData.empty<UserSocialActivityQuery.Data.Page.Activity>())
    private var followingActivityJob: Job? = null

    fun followingActivity(): StateFlow<PagingData<UserSocialActivityQuery.Data.Page.Activity>> {
        if (followingActivityJob == null) {
            // TODO: React to user changes?
            followingActivityJob = activity(followingActivity, true)
        }

        return followingActivity
    }

    fun globalActivity(): StateFlow<PagingData<UserSocialActivityQuery.Data.Page.Activity>> {
        if (globalActivityJob == null) {
            globalActivityJob = activity(globalActivity, false)
        }

        return globalActivity
    }

    fun activity(
        target: MutableStateFlow<PagingData<UserSocialActivityQuery.Data.Page.Activity>>,
        following: Boolean,
    ) = viewModelScope.launch(CustomDispatchers.IO) {
        aniListApi.authedUser.flatMapLatest { viewer ->
            refreshUptimeMillis.flatMapLatest {
                Pager(config = PagingConfig(10)) {
                    AniListPagingSource {
                        val result = aniListApi.userSocialActivity(
                            isFollowing = following,
                            page = it,
                            userIdNot = viewer?.id,
                        )
                        result.page?.pageInfo to
                                result.page?.activities?.filterNotNull().orEmpty()
                    }
                }.flow
            }
        }
            .map {
                // AniList can return duplicates across pages, manually enforce uniqueness
                val seenIds = mutableSetOf<Int>()
                it.filter {
                    when (it) {
                        is ListActivityActivity -> seenIds.add(it.id)
                        is MessageActivityActivity -> seenIds.add(it.id)
                        is TextActivityActivity -> seenIds.add(it.id)
                        is OtherActivity -> false
                    }
                }
            }
            .cachedIn(viewModelScope)
            .collectLatest(target::emit)
    }
}
