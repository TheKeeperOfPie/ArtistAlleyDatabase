package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import artistalleydatabase.modules.anime.search.generated.resources.Res
import artistalleydatabase.modules.anime.search.generated.resources.anime_home_last_added_screen_title
import artistalleydatabase.modules.anime.search.generated.resources.anime_home_suggestion_popular_all_time
import artistalleydatabase.modules.anime.search.generated.resources.anime_home_suggestion_top
import artistalleydatabase.modules.anime.search.generated.resources.anime_home_top_released_this_year_title
import artistalleydatabase.modules.anime.search.generated.resources.anime_home_trending_screen_title
import com.anilist.data.CharacterAdvancedSearchQuery
import com.anilist.data.StaffSearchQuery
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaPreview
import com.anilist.data.fragment.MediaPreviewWithDescription
import com.anilist.data.fragment.MediaWithListStatus
import com.anilist.data.fragment.StudioListRowFragment
import com.anilist.data.fragment.UserNavigationData
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.filter.CharacterSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.filter.StaffSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.studios.data.StudioEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.studios.data.filter.StudiosSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.ui.SearchMediaGenreRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.SearchMediaTagRoute
import com.thekeeperofpie.artistalleydatabase.anime.users.data.UserEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.users.data.filter.UsersSortFilterParams
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

object SearchDestinations {

    @Serializable
    data class SearchMedia(
        val title: Title? = null,
        val tagId: String? = null,
        val genre: String? = null,
        val mediaType: MediaType? = null,
        val sort: MediaSortOption? = null,
        val year: Int? = null,
        val lockSortOverride: Boolean? = null,
    ) : NavDestination {
        companion object {
            val genreRoute: SearchMediaGenreRoute = { genre, mediaType ->
                SearchMedia(
                    title = Title.Custom(genre),
                    genre = genre,
                    mediaType = mediaType,
                )
            }
            val tagRoute: SearchMediaTagRoute = { tagId, tagName, mediaType ->
                SearchMedia(
                    title = Title.Custom(tagName),
                    tagId = tagId,
                    mediaType = mediaType,
                )
            }
        }

        // TODO: Find a way to serialize StringResource
        @Serializable
        sealed interface Title {
            @Composable
            fun text(): String

            @Serializable
            data object HomeSuggestionPopularAllTime : Title {
                @Composable
                override fun text() =
                    stringResource(Res.string.anime_home_suggestion_popular_all_time)
            }

            @Serializable
            data object HomeSuggestionTop : Title {
                @Composable
                override fun text() = stringResource(Res.string.anime_home_suggestion_top)
            }

            @Serializable
            data object HomeTrending : Title {
                @Composable
                override fun text() = stringResource(Res.string.anime_home_trending_screen_title)
            }

            @Serializable
            data object HomeLastAdded : Title {
                @Composable
                override fun text() = stringResource(Res.string.anime_home_last_added_screen_title)
            }

            @Serializable
            data object HomeReleasedThisYear : Title {
                @Composable
                override fun text() =
                    stringResource(Res.string.anime_home_top_released_this_year_title)
            }

            @Serializable
            data class Custom(val title: String) : Title {
                @Composable
                override fun text() = title
            }
        }
    }

    fun <MediaPreviewEntry : Any, MediaWithListStatusEntry, CharacterEntry, StaffEntry, StudioEntry, UserEntry> addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: SearchComponent,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        unlocked: StateFlow<Boolean>,
        sortFilterStates: @Composable (SearchMedia) -> SortFilterStates<MediaPreviewEntry>,
        mediaPreviewWithDescriptionEntryProvider: MediaEntryProvider<MediaPreviewWithDescription, MediaPreviewEntry>,
        mediaWithListStatusEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaWithListStatusEntry>,
        characterEntryProvider: CharacterEntryProvider<CharacterAdvancedSearchQuery.Data.Page.Character, CharacterEntry, MediaWithListStatusEntry>,
        staffEntryProvider: StaffEntryProvider<StaffSearchQuery.Data.Page.Staff, StaffEntry, MediaWithListStatusEntry>,
        studioEntryProvider: StudioEntryProvider<StudioListRowFragment, StudioEntry, MediaWithListStatusEntry>,
        userEntryProvider: UserEntryProvider<UserNavigationData, UserEntry, MediaWithListStatusEntry>,
        onLongClickTag: (String) -> Unit,
        onLongClickGenre: (String) -> Unit,
        mediaViewOptionRow: @Composable (
            AniListViewer?,
            MediaViewOption,
            AnimeSearchEntry.Media<*>?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
    ) {
        navGraphBuilder.sharedElementComposable<SearchMedia>(navigationTypeMap) {
            val destination = it.toRoute<SearchMedia>()
            val sortFilterStates = sortFilterStates(destination)
            val viewModel = viewModel {
                component.animeSearchViewModelFactory(
                    createSavedStateHandle(),
                    unlocked,
                    sortFilterStates.animeSortFilterState.filterParams,
                    sortFilterStates.mangaSortFilterState.filterParams,
                    sortFilterStates.characterSortFilterState.filterParams,
                    sortFilterStates.staffSortFilterState.filterParams,
                    sortFilterStates.studiosSortFilterState.filterParams,
                    sortFilterStates.usersSortFilterState.filterParams,
                ).create(
                    sortFilterStates.animeFilterMedia,
                    sortFilterStates.mangaFilterMedia,
                    mediaPreviewWithDescriptionEntryProvider,
                    mediaWithListStatusEntryProvider,
                    characterEntryProvider,
                    staffEntryProvider,
                    studioEntryProvider,
                    userEntryProvider,
                )
            }
            val state = remember {
                MediaSearchScreen.State(
                    selectedType = viewModel.selectedType,
                    mediaViewOption = viewModel.mediaViewOption,
                )
            }
            val selectedType by state.selectedType.collectAsStateWithLifecycle()
            val viewer by viewModel.viewer.collectAsStateWithLifecycle()
            val mediaViewOption by viewModel.mediaViewOption.collectAsStateWithLifecycle()
            MediaSearchScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                title = destination.title,
                state = state,
                onRefresh = viewModel::onRefresh,
                content = viewModel.content.collectAsLazyPagingItems(),
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                tagId = destination.tagId,
                genre = destination.genre,
                sortFilterState = {
                    if (selectedType == SearchType.ANIME) {
                        sortFilterStates.animeSortFilterState
                    } else {
                        sortFilterStates.mangaSortFilterState
                    }
                },
                showWithSpoiler = {
                    if (selectedType == SearchType.ANIME) {
                        sortFilterStates.animeTagShowWhenSpoiler
                    } else {
                        sortFilterStates.mangaTagShowWhenSpoiler
                    }
                },
                onLongClickTag = onLongClickTag,
                onLongClickGenre = onLongClickGenre,
                item = { entry, onClickListEdit ->
                    mediaViewOptionRow(viewer, mediaViewOption, entry, onClickListEdit)
                },
            )
        }
    }

    @Stable
    class SortFilterStates<MediaPreviewEntry>(
        internal val animeSortFilterState: SortFilterState<MediaSearchFilterParams<MediaSortOption>>,
        internal val animeTagShowWhenSpoiler: MutableStateFlow<Boolean>,
        internal val animeFilterMedia: (
            result: PagingData<AnimeSearchEntry.Media<MediaPreviewEntry>>,
            transform: (AnimeSearchEntry.Media<MediaPreviewEntry>) -> MediaPreview,
        ) -> Flow<PagingData<AnimeSearchEntry.Media<MediaPreviewEntry>>>,
        internal val mangaSortFilterState: SortFilterState<MediaSearchFilterParams<MediaSortOption>>,
        internal val mangaTagShowWhenSpoiler: MutableStateFlow<Boolean>,
        internal val mangaFilterMedia: (
            result: PagingData<AnimeSearchEntry.Media<MediaPreviewEntry>>,
            transform: (AnimeSearchEntry.Media<MediaPreviewEntry>) -> MediaPreview,
        ) -> Flow<PagingData<AnimeSearchEntry.Media<MediaPreviewEntry>>>,
        internal val characterSortFilterState: SortFilterState<CharacterSortFilterParams>,
        internal val staffSortFilterState: SortFilterState<StaffSortFilterParams>,
        internal val studiosSortFilterState: SortFilterState<StudiosSortFilterParams>,
        internal val usersSortFilterState: SortFilterState<UsersSortFilterParams>,
    )
}
