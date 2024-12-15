package com.thekeeperofpie.artistalleydatabase.anime.users.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.data.UserSocialFollowersQuery
import com.anilist.data.UserSocialFollowingQuery
import com.anilist.data.fragment.PaginationInfo
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
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
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
abstract class UserSocialViewModel<T : Any>(
    private val aniListApi: AuthedAniListApi,
    private val apiCall: suspend (userId: String, page: Int) -> Pair<PaginationInfo?, List<T>>,
    private val userId: String?,
) : ViewModel() {

    private val viewer = aniListApi.authedUser
    private val refresh = RefreshFlow()

    private var data = MutableStateFlow(PagingData.empty<T>())
    private var job: Job? = null

    fun data(): StateFlow<PagingData<T>> {
        if (job == null) {
            job = viewModelScope.launch(CustomDispatchers.IO) {
                (if (userId != null) flowOf(userId) else viewer.mapNotNull { it?.id })
                    .flatMapLatest { userId ->
                        refresh.updates
                            .flatMapLatest {
                                AniListPager { apiCall(userId, it) }
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

    @Inject
    class Following(aniListApi: AuthedAniListApi, @Assisted userId: String?) :
        UserSocialViewModel<UserSocialFollowingQuery.Data.Page.Following>(
            aniListApi = aniListApi,
            apiCall = { userId, page ->
                aniListApi.userSocialFollowing(userId, page).let {
                    (it.page?.pageInfo as PaginationInfo?) to
                            it.page?.following?.filterNotNull().orEmpty()
                }
            },
            userId,
        )

    @Inject
    class Followers(aniListApi: AuthedAniListApi, @Assisted userId: String?) :
        UserSocialViewModel<UserSocialFollowersQuery.Data.Page.Follower>(
            aniListApi = aniListApi,
            apiCall = { userId, page ->
                aniListApi.userSocialFollowers(userId, page).let {
                    (it.page?.pageInfo as PaginationInfo?) to
                            it.page?.followers?.filterNotNull().orEmpty()
                }
            },
            userId,
        )
}
