package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaPreviewWithDescription
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import kotlinx.serialization.Serializable

object IgnoreDestinations {

    @Serializable
    data class Ignored(
        val mediaType: MediaType?,
    ) : NavDestination

    fun <MediaEntry : Any> addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: IgnoreComponent,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        mediaEntryProvider: MediaEntryProvider<MediaPreviewWithDescription, MediaEntry>,
        mediaViewOptionRow: @Composable (
            AniListViewer?,
            MediaViewOption,
            MediaEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
    ) {
        navGraphBuilder.sharedElementComposable<Ignored>(navigationTypeMap) {
            val viewModel = viewModel {
                component.animeMediaIgnoreViewModelFactory(createSavedStateHandle())
                    .create(mediaEntryProvider)
            }
            val viewer by viewModel.viewer.collectAsStateWithLifecycle()
            val state = remember {
                AnimeIgnoreScreen.State(
                    mediaViewOption = viewModel.mediaViewOption,
                    selectedType = viewModel.selectedType,
                    enabled = viewModel.enabled,
                    content = viewModel.content,
                )
            }
            val mediaViewOption by state.mediaViewOption.collectAsStateWithLifecycle()
            AnimeIgnoreScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                state = state,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                onRefresh = viewModel::onRefresh,
                itemKey = mediaEntryProvider::id,
                mediaViewOptionRow = { entry, onClickListEdit ->
                    mediaViewOptionRow(viewer, mediaViewOption, entry, onClickListEdit)
                },
            )
        }
    }
}
