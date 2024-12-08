package com.thekeeperofpie.artistalleydatabase.anime.user.favorite

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudioListRow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
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
class UserFavoriteStudiosViewModel(
    aniListApi: AuthedAniListApi,
    mediaListStatusController: MediaListStatusController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    @Assisted savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<AnimeDestination.UserFavoriteStudios>(navigationTypeMap)
    val userId = destination.userId
    val viewer = aniListApi.authedUser
    val studios =
        MutableStateFlow(PagingData.empty<StudioListRow.Entry<MediaWithListStatusEntry>>())

    private val refresh = RefreshFlow()

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(viewer, refresh.updates, ::Pair)
                .flatMapLatest { (viewer) ->
                    val userId = userId ?: viewer?.id
                    AniListPager {
                        val result = aniListApi.userFavoritesStudios(userId = userId!!, page = it)
                        result.user?.favourites?.studios?.pageInfo to
                                result.user?.favourites?.studios?.nodes?.filterNotNull()
                                    .orEmpty()
                    }
                }
                .mapLatest {
                    it.mapOnIO {
                        StudioListRow.Entry(
                            studio = it,
                            media = (it.main?.nodes?.filterNotNull().orEmpty()
                                .map(::MediaWithListStatusEntry) +
                                    it.nonMain?.nodes?.filterNotNull().orEmpty()
                                        .map(::MediaWithListStatusEntry))
                                .distinctBy { it.media.id }
                        )
                    }
                }
                .enforceUniqueIntIds { it.studio.id }
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
                .collectLatest(studios::emit)
        }
    }

    fun refresh() = refresh.refresh()
}
