package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.data.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager2
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffDetails
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AnimeMediaDetailsStaffViewModel(
    private val aniListApi: AuthedAniListApi,
    @Assisted media: Flow<MediaPreview?>,
) : ViewModel() {
    val staff = MutableStateFlow(PagingData.Companion.empty<StaffDetails>())

    init {
        viewModelScope.launch(CustomDispatchers.Companion.IO) {
            media.filterNotNull()
                .flowOn(CustomDispatchers.Companion.Main)
                .flatMapLatest {
                    val perPage = 6
                    AniListPager2(
                        perPage = perPage,
                        prefetchDistance = 1,
                    ) { (page, skipCache) ->
                        val result =
                            aniListApi.mediaDetailsStaffPage(
                                it.id.toString(),
                                page,
                                perPage,
                                skipCache
                            ).staff
                        result.pageInfo to result.edges.filterNotNull().map {
                            StaffDetails(
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
