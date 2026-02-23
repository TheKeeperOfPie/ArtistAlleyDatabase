package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.thekeeperofpie.artistalleydatabase.alley.edit.admin.AdminScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistAddScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistHistoryScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistListScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form.ArtistFormHistoryScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form.ArtistFormMergeScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form.ArtistFormQueueScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImagesEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.merch.MerchEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.merch.MerchListScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.merch.MerchResolutionScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.ArtistAlleyEditTopLevelStacks
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.TopLevelStackKey
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.rememberArtistAlleyEditTopLevelStacks
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.rememberDecoratedNavEntries
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.StampRallyAddScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.StampRallyEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.StampRallyHistoryScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.StampRallyListScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.form.StampRallyFormHistoryScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.form.StampRallyFormMergeScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.form.StampRallyFormQueueScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.series.SeriesEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.series.SeriesListScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.series.SeriesResolutionScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagResolutionQueueScreen
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementEntry
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArtistAlleyEditApp(
    graph: ArtistAlleyEditGraph,
    navStack: ArtistAlleyEditTopLevelStacks = rememberArtistAlleyEditTopLevelStacks(),
    onDebugOpenForm: (formLink: String) -> Unit = {},
) {
    CompositionLocalProvider(LocalNavigationController provides remember {
        object : NavigationController {
            override fun navigateUp(): Boolean = false
            override fun navigate(navDestination: NavDestination) {}
            override fun popBackStack() = false
            override fun popBackStack(navDestination: NavDestination) = false
        }
    }) {
        SharedTransitionLayout {
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this,
                LocalNavigationResults provides rememberNavigationResults(),
            ) {
                val navigationEventDispatcherOwner = LocalNavigationEventDispatcherOwner.current
                val onClickBackInput = remember { DirectNavigationEventInput() }
                DisposableEffect(onClickBackInput) {
                    val dispatcher = navigationEventDispatcherOwner?.navigationEventDispatcher
                        ?: return@DisposableEffect onDispose {}
                    dispatcher.addInput(onClickBackInput)
                    onDispose { dispatcher.removeInput(onClickBackInput) }
                }
                DisposableEffect(navigationEventDispatcherOwner, navStack) {
                    navigationEventDispatcherOwner?.navigationEventDispatcher
                        ?.addHandler(navStack)
                    onDispose { navStack.remove() }
                }

                val onClickBack = { force: Boolean ->
                    if (force) {
                        navStack.onBack()
                    } else {
                        onClickBackInput.backCompleted()
                    }
                }
                val entryProvider = entryProvider(
                    graph = graph,
                    navStack = navStack,
                    onClickBack = onClickBack,
                    onDebugOpenForm = onDebugOpenForm,
                )

                val decoratedNavEntries = rememberDecoratedNavEntries(navStack, entryProvider)

                NavigationSuiteScaffold(
                    navigationSuiteItems = {
                        TopLevelStackKey.entries.forEachIndexed { index, key ->
                            item(
                                icon = {
                                    Icon(
                                        imageVector = key.icon,
                                        contentDescription = stringResource(key.title)
                                    )
                                },
                                label = { Text(stringResource(key.title)) },
                                selected = navStack.topLevelStackIndex == index,
                                onClick = { navStack.moveToTopLevelStack(index) },
                                modifier = Modifier.zIndex(3f)
                            )
                        }
                    }
                ) {
                    NavDisplay(
                        entries = navStack.calculateBackStack(decoratedNavEntries),
                        onBack = navStack::onBack,
                        transitionSpec = {
                            slideInHorizontally(initialOffsetX = { it }) togetherWith fadeOut()
                        },
                        popTransitionSpec = {
                            fadeIn() togetherWith slideOutHorizontally(targetOffsetX = { it })
                        },
                        predictivePopTransitionSpec = {
                            slideInHorizontally(initialOffsetX = { it }) togetherWith fadeOut()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun entryProvider(
    graph: ArtistAlleyEditGraph,
    navStack: ArtistAlleyEditTopLevelStacks,
    onClickBack: (force: Boolean) -> Unit,
    onDebugOpenForm: (formLink: String) -> Unit,
) = entryProvider<NavKey> {
    sharedElementEntry<AlleyEditDestination.Home> {
        ArtistListScreen(
            graph = graph,
            onAddArtist = {
                navStack.navigate(AlleyEditDestination.ArtistAdd(it))
            },
            onEditArtist = { dataYear, artistId ->
                navStack.navigate(
                    AlleyEditDestination.ArtistEdit(
                        dataYear,
                        artistId
                    )
                )
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.Admin> {
        AdminScreen(graph = graph, onDebugOpenForm = onDebugOpenForm)
    }
    sharedElementEntry<AlleyEditDestination.ArtistAdd> { route ->
        ArtistAddScreen(
            dataYear = route.dataYear,
            artistId = route.artistId,
            graph = graph,
            onClickBack = onClickBack,
            onClickEditImages = { displayName, images ->
                navStack.navigate(
                    AlleyEditDestination.ImagesEdit(displayName, images)
                )
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.ArtistEdit> { route ->
        ArtistEditScreen(
            dataYear = route.dataYear,
            artistId = route.artistId,
            graph = graph,
            onClickBack = onClickBack,
            onClickEditImages = { displayName, images ->
                navStack.navigate(
                    AlleyEditDestination.ImagesEdit(displayName, images)
                )
            },
            onClickMerge = {
                navStack.navigate(
                    AlleyEditDestination.ArtistFormMerge(
                        dataYear = route.dataYear,
                        artistId = route.artistId,
                    )
                )
            },
            onClickHistory = {
                navStack.navigate(
                    AlleyEditDestination.ArtistHistory(
                        dataYear = route.dataYear,
                        artistId = route.artistId,
                    )
                )
            },
            onClickDebugForm = onDebugOpenForm,
        )
    }
    sharedElementEntry<AlleyEditDestination.ArtistFormHistory> { route ->
        ArtistFormHistoryScreen(
            dataYear = route.dataYear,
            artistId = route.artistId,
            formTimestamp = route.formTimestamp,
            graph = graph,
            onClickBack = onClickBack,
        )
    }
    sharedElementEntry<AlleyEditDestination.ArtistFormMerge> { route ->
        ArtistFormMergeScreen(
            dataYear = route.dataYear,
            artistId = route.artistId,
            graph = graph,
            onClickBack = onClickBack,
        )
    }
    sharedElementEntry<AlleyEditDestination.ArtistFormQueue> {
        ArtistFormQueueScreen(
            graph = graph,
            onSelectEntry = {
                // TODO: Support other conventions?
                navStack.navigate(
                    AlleyEditDestination.ArtistFormMerge(
                        dataYear = DataYear.ANIME_EXPO_2026,
                        artistId = it,
                    )
                )
            },
            onSelectHistoryEntry = { artistId, formTimestamp ->
                // TODO: Support other conventions?
                navStack.navigate(
                    AlleyEditDestination.ArtistFormHistory(
                        dataYear = DataYear.ANIME_EXPO_2026,
                        artistId = artistId,
                        formTimestamp = formTimestamp,
                    )
                )
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.ArtistHistory> { route ->
        ArtistHistoryScreen(
            dataYear = route.dataYear,
            artistId = route.artistId,
            graph = graph,
            onClickBack = onClickBack,
        )
    }
    sharedElementEntry<AlleyEditDestination.ImagesEdit> {
        ImagesEditScreen(route = it, graph = graph, onClickBack = onClickBack)
    }
    sharedElementEntry<AlleyEditDestination.Series> {
        SeriesListScreen(
            graph = graph,
            onClickEditSeries = { seriesInfo, seriesColumn ->
                navStack.navigate(AlleyEditDestination.SeriesEdit(seriesInfo, seriesColumn))
            },
            onClickAddSeries = {
                navStack.navigate(AlleyEditDestination.SeriesAdd())
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.SeriesAdd> {
        SeriesEditScreen(
            seriesId = it.seriesId,
            initialInfo = null,
            graph = graph,
            onClickBack = onClickBack,
        )
    }
    sharedElementEntry<AlleyEditDestination.SeriesEdit> {
        SeriesEditScreen(
            seriesId = it.series.uuid,
            initialInfo = it,
            graph = graph,
            onClickBack = onClickBack,
        )
    }
    sharedElementEntry<AlleyEditDestination.Merch> {
        MerchListScreen(
            graph = graph,
            onClickEditMerch = {
                navStack.navigate(AlleyEditDestination.MerchEdit(it))
            },
            onClickAddMerch = {
                navStack.navigate(AlleyEditDestination.MerchAdd())
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.MerchAdd> {
        MerchEditScreen(
            merchId = it.merchId,
            initialInfo = null,
            graph = graph,
            onClickBack = onClickBack,
        )
    }
    sharedElementEntry<AlleyEditDestination.MerchEdit> {
        MerchEditScreen(
            merchId = it.merch.uuid,
            initialInfo = it.merch,
            graph = graph,
            onClickBack = onClickBack,
        )
    }
    sharedElementEntry<AlleyEditDestination.TagResolution> {
        TagResolutionQueueScreen(
            graph = graph,
            onClickSeries = {
                navStack.navigate(AlleyEditDestination.SeriesResolution(it))
            },
            onClickMerch = {
                navStack.navigate(AlleyEditDestination.MerchResolution(it))
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.SeriesResolution> {
        SeriesResolutionScreen(
            seriesId = it.seriesId,
            graph = graph,
            onClickBack = { onClickBack(true) },
        )
    }
    sharedElementEntry<AlleyEditDestination.MerchResolution> {
        MerchResolutionScreen(
            merchId = it.merchId,
            graph = graph,
            onClickBack = { onClickBack(true) },
        )
    }
    sharedElementEntry<AlleyEditDestination.StampRallies> {
        StampRallyListScreen(
            graph = graph,
            onAddStampRally = {
                navStack.navigate(AlleyEditDestination.StampRallyAdd(it))
            },
            onEditStampRally = { dataYear, stampRallyId ->
                navStack.navigate(AlleyEditDestination.StampRallyEdit(dataYear, stampRallyId))
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.StampRallyAdd> { route ->
        StampRallyAddScreen(
            dataYear = route.dataYear,
            stampRallyId = route.stampRallyId,
            graph = graph,
            onClickBack = onClickBack,
        )
    }
    sharedElementEntry<AlleyEditDestination.StampRallyEdit> { route ->
        StampRallyEditScreen(
            dataYear = route.dataYear,
            stampRallyId = route.stampRallyId,
            graph = graph,
            onClickBack = onClickBack,
            onClickEditImages = { _, _ ->
                // TODO: Open images screen
            },
            onClickHistory = {
                navStack.navigate(
                    AlleyEditDestination.StampRallyHistory(
                        dataYear = route.dataYear,
                        stampRallyId = route.stampRallyId,
                    )
                )
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.StampRallyHistory> { route ->
        StampRallyHistoryScreen(
            dataYear = route.dataYear,
            stampRallyId = route.stampRallyId,
            graph = graph,
            onClickBack = onClickBack,
        )
    }
    sharedElementEntry<AlleyEditDestination.StampRallyFormQueue> {
        StampRallyFormQueueScreen(
            graph = graph,
            onSelectEntry = { artistId, stampRallyId ->
                navStack.navigate(
                    AlleyEditDestination.StampRallyFormMerge(
                        dataYear = DataYear.ANIME_EXPO_2026,
                        artistId = artistId,
                        stampRallyId = stampRallyId,
                    )
                )
            },
            onSelectHistoryEntry = { artistId, stampRallyId, formTimestamp ->
                navStack.navigate(
                    AlleyEditDestination.StampRallyFormHistory(
                        dataYear = DataYear.ANIME_EXPO_2026,
                        artistId = artistId,
                        stampRallyId = stampRallyId,
                        formTimestamp = formTimestamp,
                    )
                )
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.StampRallyFormMerge> { route ->
        StampRallyFormMergeScreen(
            dataYear = route.dataYear,
            artistId = route.artistId,
            stampRallyId = route.stampRallyId,
            graph = graph,
            onClickBack = onClickBack,
        )
    }
    sharedElementEntry<AlleyEditDestination.StampRallyFormHistory> { route ->
        StampRallyFormHistoryScreen(
            dataYear = route.dataYear,
            artistId = route.artistId,
            stampRallyId = route.stampRallyId,
            formTimestamp = route.formTimestamp,
            graph = graph,
            onClickBack = onClickBack,
        )
    }
}
