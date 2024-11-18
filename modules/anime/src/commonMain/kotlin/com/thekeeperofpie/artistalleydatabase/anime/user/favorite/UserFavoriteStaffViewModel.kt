package com.thekeeperofpie.artistalleydatabase.anime.user.favorite

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffListRow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class UserFavoriteStaffViewModel(
    aniListApi: AuthedAniListApi,
    mediaListStatusController: MediaListStatusController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val userId = savedStateHandle.get<String?>("userId")
    val viewer = aniListApi.authedUser
    val staff = MutableStateFlow(PagingData.empty<StaffListRow.Entry>())

    private val refresh = RefreshFlow()

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                viewer,
                refresh.updates,
                ::Pair,
            ).flatMapLatest { (viewer) ->
                val userId = userId ?: viewer?.id
                AniListPager {
                    val result = aniListApi.userFavoritesStaff(userId = userId!!, page = it)
                    result.user?.favourites?.staff?.pageInfo to
                            result.user?.favourites?.staff?.nodes?.filterNotNull()
                                .orEmpty()
                }
            }
                .mapLatest {
                    it.mapOnIO {
                        StaffListRow.Entry(
                            staff = it,
                            media = it.staffMedia?.nodes?.filterNotNull().orEmpty()
                                .distinctBy { it.id }
                                .map(::MediaWithListStatusEntry)
                        )
                    }
                }
                .enforceUniqueIntIds { it.staff.id }
                .cachedIn(viewModelScope)
                .flatMapLatest {
                    combine(
                        mediaListStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { statuses, _, filteringData ->
                        it.mapNotNull {
                            it.copy(media = it.media.mapNotNull {
                                applyMediaFiltering(
                                    statuses = statuses,
                                    ignoreController = ignoreController,
                                    filteringData = filteringData,
                                    entry = it,
                                    filterableData = it.mediaFilterable,
                                    copy = { copy(mediaFilterable = it) },
                                )
                            })
                        }
                    }
                }
                .cachedIn(viewModelScope)
                .collectLatest(staff::emit)
        }
    }

    fun refresh() = refresh.refresh()
}
