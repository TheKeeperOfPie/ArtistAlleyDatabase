package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.anilist.data.fragment.CharacterWithRoleAndFavorites
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaWithListStatus
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterDetails
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.staff.character.StaffCharactersScreen
import com.thekeeperofpie.artistalleydatabase.anime.staff.details.StaffDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.staff.details.StaffMediaTimeline
import com.thekeeperofpie.artistalleydatabase.anime.staff.details.StaffTimeline
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffDetailsRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

object StaffDestinations {

    @Serializable
    data class StaffCharacters(
        val staffId: String,
        val sharedTransitionKey: SharedTransitionKey? = null,
        val headerParams: StaffHeaderParams? = null,
    ) : NavDestination

    @Serializable
    data class StaffDetails(
        val staffId: String,
        val sharedTransitionKey: SharedTransitionKey? = null,
        val headerParams: StaffHeaderParams? = null,
    ) : NavDestination {
        companion object {
            val route: StaffDetailsRoute =
                { id, sharedTransitionKey, name, subtitle, image, favorite ->
                    StaffDetails(
                        staffId = id,
                        sharedTransitionKey = sharedTransitionKey,
                        headerParams = StaffHeaderParams(
                            name = name,
                            subtitle = subtitle,
                            coverImage = image,
                            favorite = favorite,
                        )
                    )
                }
        }
    }

    fun <CharacterEntry : Any, MediaEntry> addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: StaffComponent,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        characterRow: @Composable (
            CharacterEntry?,
            AniListViewer?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        charactersSection: LazyGridScope.(
            titleRes: StringResource,
            viewAllRoute: () -> NavDestination,
            viewAllContentDescriptionTextRes: StringResource,
            characters: LazyPagingItems<CharacterDetails>,
        ) -> Unit,
        characterCard: @Composable LazyItemScope.(StaffMediaTimeline.Character) -> Unit,
        mediaGridCard: @Composable (
            StaffTimeline.MediaWithRole<MediaEntry>,
            AniListViewer?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        characterEntryProvider: CharacterEntryProvider<CharacterWithRoleAndFavorites, CharacterEntry, MediaEntry>,
        mediaEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaEntry>,
    ) {
        navGraphBuilder.sharedElementComposable<StaffCharacters>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val viewModel = viewModel {
                component.staffCharactersViewModelFactory(createSavedStateHandle())
                    .create(characterEntryProvider, mediaEntryProvider)
            }
            val destination = it.toRoute<StaffCharacters>()
            val headerValues = StaffHeaderValues(
                params = destination.headerParams,
                staff = { viewModel.entry.result },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            val viewer by viewModel.viewer.collectAsState()
            StaffCharactersScreen(
                staffId = viewModel.staffId,
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                sortFilterState = viewModel.sortFilterController::state,
                onRefresh = viewModel::refresh,
                characters = viewModel.items.collectAsLazyPagingItems(),
                characterItemKey = characterEntryProvider::id,
                characterRow = { entry, onClickListEdit ->
                    characterRow(entry, viewer, onClickListEdit)
                },
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                headerValues = headerValues,
                sharedTransitionKey = destination.sharedTransitionKey,
                onFavoriteChanged = {
                    viewModel.favoritesToggleHelper
                        .set(FavoriteType.STAFF, viewModel.staffId, it)
                },
            )
        }

        navGraphBuilder.sharedElementComposable<StaffDetails>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/staff/{staffId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/staff/{staffId}/.*" },
            ),
        ) {
            val viewModel = viewModel {
                component.staffDetailsViewModelFactory(createSavedStateHandle())
                    .create(mediaEntryProvider)
            }
            val destination = it.toRoute<StaffDetails>()
            val entry by viewModel.entry.collectAsState()
            val headerValues = StaffHeaderValues(
                params = destination.headerParams,
                staff = { entry.result?.staff },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            val viewer by viewModel.viewer.collectAsState()
            StaffDetailsScreen(
                staffId = viewModel.staffId,
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                onRefresh = viewModel::refresh,
                headerValues = headerValues,
                sharedTransitionKey = destination.sharedTransitionKey,
                entry = { entry },
                mediaTimeline = viewModel.mediaTimeline,
                staffTimeline = viewModel.staffTimeline,
                characters = { viewModel.characters.collectAsLazyPagingItems() },
                favorite = {
                    viewModel.favoritesToggleHelper.favorite ?: entry.result?.staff?.isFavourite
                },
                onFavoriteChanged = {
                    viewModel.favoritesToggleHelper
                        .set(FavoriteType.STAFF, viewModel.staffId, it)
                },
                onRequestMediaYear = viewModel::onRequestMediaYear,
                onRequestStaffYear = viewModel::onRequestStaffYear,
                charactersSection = charactersSection,
                characterCard = characterCard,
                mediaGridCard = { mediaWithRole, onClickListEdit ->
                    mediaGridCard(mediaWithRole, viewer, onClickListEdit)
                },
            )
        }
    }
}
