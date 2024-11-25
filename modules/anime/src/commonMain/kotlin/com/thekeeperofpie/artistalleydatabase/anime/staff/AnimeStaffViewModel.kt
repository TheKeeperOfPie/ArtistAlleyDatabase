package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager2
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AnimeStaffViewModel(
    private val aniListApi: AuthedAniListApi,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted mediaDetailsViewModel: AnimeMediaDetailsViewModel,
) : ViewModel() {
    val staff = MutableStateFlow(PagingData.empty<DetailsStaff>())

    private val mediaId = savedStateHandle.get<String>("mediaId")!!

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                mediaDetailsViewModel.refresh.updates,
                snapshotFlow { mediaDetailsViewModel.state.mediaEntry.result }
                    .filterNotNull(),
            ) { it, _ -> it }
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { refresh ->
                    val perPage = 6
                    AniListPager2(
                        perPage = perPage,
                        prefetchDistance = 1,
                        skipCache = refresh.fromUser,
                    ) { (page, skipCache) ->
                        val result =
                            aniListApi.mediaDetailsStaffPage(
                                mediaId,
                                page,
                                perPage,
                                skipCache
                            ).staff
                        result.pageInfo to result.edges.filterNotNull().map {
                            DetailsStaff(
                                id = it.node.id.toString(),
                                name = it.node.name,
                                image = it.node.image?.large,
                                role = it.role,
                                staff = it.node,
                            )
                        }
                    }
                }
                .enforceUniqueIds { it.idWithRole }
                .cachedIn(viewModelScope)
                .collectLatest(staff::emit)
        }
    }
}
