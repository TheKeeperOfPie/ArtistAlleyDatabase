package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import kotlinx.serialization.Serializable

object AnimeNewsDestinations {

    @Serializable
    data object News : NavDestination

    fun addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: AnimeNewsComponent,
    ) {
        navGraphBuilder.sharedElementComposable<AnimeNewsDestinations.News>(navigationTypeMap) {
            val navigationController = LocalNavigationController.current
            val fullscreenImageHandler = LocalFullscreenImageHandler.current
            val newsSortFilterViewModel = viewModel {
                component.newsSortFilterViewModelFactory.create(createSavedStateHandle())
            }
            val viewModel =
                viewModel { component.animeNewsViewModelFactory.create(newsSortFilterViewModel) }
            AnimeNewsScreen(
                viewModel = viewModel,
                sortFilterState = newsSortFilterViewModel.state,
                onBackClick = navigationController::navigateUp,
                onOpenImage = fullscreenImageHandler::openImage,
            )
        }
    }
}
