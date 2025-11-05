package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlin.uuid.Uuid

@Composable
fun ArtistAlleyEditApp(
    graph: ArtistAlleyEditGraph,
    twoWayStack: ArtistAlleyEditTwoWayStack = rememberArtistAlleyEditTwoWayStack(),
) {
    Scaffold { paddingValues ->
        NavDisplay(
            entryDecorators = listOf(
                rememberTwoWaySaveableStateHolder(twoWayStack),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            backStack = twoWayStack.navBackStack,
            onBack = twoWayStack::onBack,
            entryProvider = entryProvider {
                entry<AlleyEditDestination.Home> {
                    Box(Modifier.size(400.dp).background(Color.Red).clickable {
                        twoWayStack.navigate(AlleyEditDestination.ArtistDetails(Uuid.random()))
                    })
                }
                entry<AlleyEditDestination.ArtistDetails> {
                    val viewModel = viewModel {
                        graph.artistDetailsViewModelFactory.create(it, createSavedStateHandle())
                    }
                    Box(Modifier.size(400.dp).background(Color.Blue).clickable {
                        twoWayStack.navigate(AlleyEditDestination.ArtistDetails(Uuid.random()))
                    }) {
                        Text(viewModel.artistId.toString())
                    }
                }
            },
            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it })
            },
            popTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}
