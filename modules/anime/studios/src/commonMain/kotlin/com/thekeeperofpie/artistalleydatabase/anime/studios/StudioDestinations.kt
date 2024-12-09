package com.thekeeperofpie.artistalleydatabase.anime.studios

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import kotlinx.serialization.Serializable

object StudioDestinations {

    @Serializable
    data class StudioMedias(
        val studioId: String,
        val name: String? = null,
        // TODO: Favorite is never actually passed in
        val favorite: Boolean? = null,
    ) : NavDestination

    fun <MediaEntry : Any> addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: StudiosComponent,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        mediaEntryProvider: MediaEntryProvider<MediaPreview, MediaEntry>,
        mediaRow: @Composable (
            MediaEntry?,
            viewer: AniListViewer?,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
    ) {
        navGraphBuilder.sharedElementComposable<StudioMedias>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/studio/{studioId}" },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/studio/{studioId}/.*"
                },
            ),
        ) {
            val destination = it.toRoute<StudioMedias>()
            val viewModel = viewModel {
                component.studioMediasViewModelFactory(createSavedStateHandle())
                    .create(mediaEntryProvider)
            }

            val viewer by viewModel.viewer.collectAsState()
            StudioMediasScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                sortFilterState = viewModel.sortFilterController::state,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                onRefresh = viewModel::refresh,
                media = viewModel.items.collectAsLazyPagingItems(),
                mediaItemKey = { mediaEntryProvider.mediaFilterable(it).mediaId },
                mediaRow = { entry, onClickListEdit, modifier ->
                    mediaRow(entry, viewer, onClickListEdit, modifier)
                },
                name = { viewModel.entry.result?.studio?.name ?: destination.name ?: "" },
                favorite = {
                    viewModel.favoritesToggleHelper.favorite
                        ?: viewModel.entry.result?.studio?.isFavourite
                        ?: destination.favorite
                },
                onFavoriteChanged = {
                    viewModel.favoritesToggleHelper.set(
                        type = FavoriteType.STUDIO,
                        id = viewModel.studioId,
                        favorite = it,
                    )
                }
            )
        }
    }
}
