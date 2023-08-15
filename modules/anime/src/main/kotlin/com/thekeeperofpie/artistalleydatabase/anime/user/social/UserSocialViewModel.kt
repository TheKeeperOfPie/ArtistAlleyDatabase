package com.thekeeperofpie.artistalleydatabase.anime.user.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.UserSocialFollowersQuery
import com.anilist.UserSocialFollowingQuery
import com.anilist.fragment.PaginationInfo
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
abstract class UserSocialViewModel<T : Any>(
    private val aniListApi: AuthedAniListApi,
    private val apiCall: suspend (userId: String, page: Int) -> Pair<PaginationInfo?, List<T>>
) : ViewModel() {

    private val viewer = aniListApi.authedUser
    private val refreshUptimeMillis = MutableStateFlow(-1L)

    private var data = MutableStateFlow(PagingData.empty<T>())
    private var job: Job? = null

    private var userId: String? = null

    fun initialize(userId: String?) {
        this.userId = userId
    }

    fun data(): StateFlow<PagingData<T>> {
        if (job == null) {
            job = viewModelScope.launch(CustomDispatchers.IO) {
                val userId = userId
                (if (userId != null) flowOf(userId) else viewer.mapNotNull { it?.id })
                    .flatMapLatest { userId ->
                        refreshUptimeMillis
                            .flatMapLatest {
                                Pager(PagingConfig(pageSize = 10)) {
                                    AniListPagingSource { apiCall(userId, it) }
                                }.flow
                            }
                    }
                    .catch {
                        // TODO: Error message
                    }
                    .cachedIn(viewModelScope)
                    .collectLatest(data::emit)
            }
        }

        return data
    }

    @HiltViewModel
    class Following @Inject constructor(aniListApi: AuthedAniListApi) :
        UserSocialViewModel<UserSocialFollowingQuery.Data.Page.Following>(
            aniListApi = aniListApi,
            apiCall = { userId, page ->
                aniListApi.userSocialFollowing(userId, page).let {
                    (it.page?.pageInfo as PaginationInfo?) to
                            it.page?.following?.filterNotNull().orEmpty()
                }
            }
        )

    @HiltViewModel
    class Followers @Inject constructor(aniListApi: AuthedAniListApi) :
        UserSocialViewModel<UserSocialFollowersQuery.Data.Page.Follower>(
            aniListApi = aniListApi,
            apiCall = { userId, page ->
                aniListApi.userSocialFollowers(userId, page).let {
                    (it.page?.pageInfo as PaginationInfo?) to
                            it.page?.followers?.filterNotNull().orEmpty()
                }
            }
        )
}
