package com.thekeeperofpie.artistalleydatabase.anime.characters

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.characters.details.CharacterDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.characters.media.CharacterMediasScreen
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffDetails
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavHostController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object CharacterDestinations {

    @Serializable
    data class CharacterDetails(
        val characterId: String,
        val sharedTransitionScopeKey: String? = null,
        val headerParams: CharacterHeaderParams? = null,
    ) : NavDestination

    @Serializable
    data class CharacterMedias(
        val characterId: String,
        val sharedTransitionKey: SharedTransitionKey? = null,
        val headerParams: CharacterHeaderParams? = null,
    ) : NavDestination

    fun <MediaEntry : Any> addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: CharactersComponent,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        mediaRow: @Composable (
            MediaEntry?,
            viewer: AniListViewer?,
            label: (@Composable () -> Unit)?,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
        staffSection: LazyGridScope.(
            titleRes: StringResource?,
            LazyPagingItems<StaffDetails>,
            roleLines: Int,
        ) -> Unit,
        mediaEntryProvider: MediaEntryProvider<MediaPreview, MediaEntry>,
    ) {
        navGraphBuilder.sharedElementComposable<CharacterDetails>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/character/{characterId}"
                },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/character/{characterId}/.*"
                },
            ),
        ) {
            val viewModel = viewModel {
                component.animeCharacterDetailsViewModelFactory(createSavedStateHandle())
                    .create(mediaEntryProvider)
            }
            val destination = it.toRoute<CharacterDestinations.CharacterDetails>()
            val headerValues = CharacterHeaderValues(
                params = destination.headerParams,
                character = { viewModel.entry.result?.character },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            val viewer by viewModel.viewer.collectAsState()
            SharedTransitionKeyScope(destination.sharedTransitionScopeKey) {
                val navHostController = LocalNavHostController.current
                CharacterDetailsScreen(
                    mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                    characterId = viewModel.characterId,
                    headerValues = headerValues,
                    viewer = { viewer },
                    upIconOption = UpIconOption.Back(navHostController),
                    onRefresh = viewModel::refresh,
                    entry = { viewModel.entry },
                    onFavoriteChanged = {
                        viewModel.favoritesToggleHelper
                            .set(FavoriteType.CHARACTER, viewModel.characterId, it)
                    },
                    voiceActorsDeferred = { viewModel.voiceActorsDeferred },
                    staffSection = staffSection,
                    mediaRow = { item, onClickListEdit, modifier ->
                        val label = item.characterRole?.let {
                            @Composable {
                                Text(
                                    text = stringResource(it.toTextRes()),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.surfaceTint,
                                    modifier = Modifier
                                        .wrapContentHeight()
                                        .padding(
                                            start = 12.dp,
                                            top = 10.dp,
                                            end = 16.dp,
                                        )
                                )
                            }
                        }

                        mediaRow(item.mediaEntry, viewer, label, onClickListEdit, modifier)
                    }
                )
            }
        }

        navGraphBuilder.sharedElementComposable<CharacterMedias>(
            navigationTypeMap = navigationTypeMap
        ) {
            val destination = it.toRoute<CharacterMedias>()
            val viewModel = viewModel {
                component.characterMediasViewModelFactory(createSavedStateHandle())
                    .create(mediaEntryProvider)
            }
            val headerValues = CharacterHeaderValues(
                params = destination.headerParams,
                character = { viewModel.entry.result?.character },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            val viewer by viewModel.viewer.collectAsState()
            CharacterMediasScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                sortFilterState = { viewModel.sortFilterController.state },
                characterId = viewModel.characterId,
                headerValues = headerValues,
                sharedTransitionKey = destination.sharedTransitionKey,
                viewer = { viewer },
                onRefresh = viewModel::refresh,
                items = viewModel.items.collectAsLazyPagingItems(),
                itemKey = { mediaEntryProvider.mediaFilterable(it).mediaId },
                onFavoriteChanged = {
                    viewModel.favoritesToggleHelper
                        .set(FavoriteType.CHARACTER, viewModel.characterId, it)
                },
                mediaRow = { entry, onClickListEdit, modifier ->
                    mediaRow(entry, viewer, null, onClickListEdit, modifier)
                },
            )
        }
    }
}
