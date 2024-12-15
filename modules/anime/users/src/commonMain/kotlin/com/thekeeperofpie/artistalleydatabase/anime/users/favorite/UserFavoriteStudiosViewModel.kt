package com.thekeeperofpie.artistalleydatabase.anime.users.favorite

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.data.fragment.MediaWithListStatus
import com.anilist.data.fragment.StudioListRowFragment
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.studios.data.StudioEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.users.UserDestinations
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
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
class UserFavoriteStudiosViewModel<MediaEntry : Any, StudioEntry : Any>(
    aniListApi: AuthedAniListApi,
    mediaListStatusController: MediaListStatusController,
    ignoreController: IgnoreController,
    settings: MediaDataSettings,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted private val mediaEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaEntry>,
    @Assisted private val studioEntryProvider: StudioEntryProvider<StudioListRowFragment, StudioEntry, MediaEntry>,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<UserDestinations.UserFavoriteStudios>(navigationTypeMap)
    val userId = destination.userId
    val viewer = aniListApi.authedUser
    val studios = MutableStateFlow(PagingData.Companion.empty<StudioEntry>())

    private val refresh = RefreshFlow()

    init {
        viewModelScope.launch(CustomDispatchers.Companion.IO) {
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
                        val media = (it.main?.nodes?.filterNotNull().orEmpty()
                                + it.nonMain?.nodes?.filterNotNull().orEmpty())
                            .distinctBy { it.id }
                            .map(mediaEntryProvider::mediaEntry)
                        studioEntryProvider.studioEntry(it, media)
                    }
                }
                .enforceUniqueIds(studioEntryProvider::id)
                .cachedIn(viewModelScope)
                .flatMapLatest {
                    combine(
                        mediaListStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { statuses, _, filteringData ->
                        it.mapNotNull {
                            studioEntryProvider.copyStudioEntry(
                                it,
                                media = studioEntryProvider.media(it).mapNotNull {
                                    applyMediaFiltering(
                                        statuses = statuses,
                                        ignoreController = ignoreController,
                                        filteringData = filteringData,
                                        entry = it,
                                        filterableData = mediaEntryProvider.mediaFilterable(it),
                                        copy = { mediaEntryProvider.copyMediaEntry(this, it) },
                                    )
                                }
                            )
                        }
                    }
                }
                .cachedIn(viewModelScope)
                .collectLatest(studios::emit)
        }
    }

    fun refresh() = refresh.refresh()

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val mediaListStatusController: MediaListStatusController,
        private val ignoreController: IgnoreController,
        private val settings: MediaDataSettings,
        private val navigationTypeMap: NavigationTypeMap,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) {
        fun <MediaEntry : Any, StudioEntry : Any> create(
            mediaEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaEntry>,
            studioEntryProvider: StudioEntryProvider<StudioListRowFragment, StudioEntry, MediaEntry>,
        ) = UserFavoriteStudiosViewModel(
            aniListApi = aniListApi,
            mediaListStatusController = mediaListStatusController,
            ignoreController = ignoreController,
            settings = settings,
            navigationTypeMap = navigationTypeMap,
            savedStateHandle = savedStateHandle,
            mediaEntryProvider = mediaEntryProvider,
            studioEntryProvider = studioEntryProvider,
        )
    }
}
