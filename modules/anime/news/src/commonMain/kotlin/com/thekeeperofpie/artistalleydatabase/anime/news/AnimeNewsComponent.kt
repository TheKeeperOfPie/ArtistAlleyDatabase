package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.lifecycle.viewmodel.compose.viewModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavHostController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestinationProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface AnimeNewsComponent {
    val animeNewsViewModel: () -> AnimeNewsViewModel

    @Provides
    @IntoSet
    fun provideNewsDestination(animeNewsViewModel: () -> AnimeNewsViewModel) =
        NavDestinationProvider {
            sharedElementComposable<AnimeNewsNavDestinations.News>(it) {
                val navHostController = LocalNavHostController.current
                val fullscreenImageHandler = LocalFullscreenImageHandler.current
                AnimeNewsScreen(
                    viewModel = viewModel { animeNewsViewModel() },
                    onBackClick = navHostController::navigateUp,
                    onOpenImage = fullscreenImageHandler::openImage,
                )
            }
        }
}
