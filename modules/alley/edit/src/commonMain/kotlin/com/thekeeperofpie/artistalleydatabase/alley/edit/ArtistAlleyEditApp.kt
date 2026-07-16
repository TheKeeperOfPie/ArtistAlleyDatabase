package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.zIndex
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_top_level_nav_more
import com.thekeeperofpie.artistalleydatabase.alley.edit.admin.AdminScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistAddScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistHistoryScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistListScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form.ArtistFormHistoryScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form.ArtistFormMergeScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form.ArtistFormQueueScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.catalog.ArtistCatalogsQueueScreen
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
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.links.StampRallyLinksQueueScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.remote.RemoteArtistDataHistoryMergeScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.remote.RemoteArtistDataMergeScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.remote.RemoteArtistDataQueueScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.series.SeriesEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.series.SeriesListScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.series.SeriesResolutionScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagResolutionQueueScreen
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.MoreVert
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementEntry
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArtistAlleyEditApp(
    graph: ArtistAlleyEditGraph,
    navStack: ArtistAlleyEditTopLevelStacks = rememberArtistAlleyEditTopLevelStacks(),
    onDebugOpenForm: (formLink: String) -> Unit = {},
) {
    val lastViewedConnection = graph.lastViewedConnection
    LaunchedEffect(lastViewedConnection) {
        snapshotFlow { navStack.navBackStack().lastOrNull() }
            .filterIsInstance<AlleyEditDestination>()
            .collectLatest(lastViewedConnection::onPageView)
    }
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

                val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
                    currentWindowAdaptiveInfo()
                )
                var showOverflow by remember { mutableStateOf(false) }
                NavigationSuiteScaffold(
                    layoutType = layoutType,
                    navigationSuiteItems = {
                        navItems(
                            layoutType = layoutType,
                            navStack = navStack,
                            showOverflow = { showOverflow },
                            onChangeOverflow = { showOverflow = it },
                        )
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

private fun NavigationSuiteScope.navItems(
    layoutType: NavigationSuiteType,
    navStack: ArtistAlleyEditTopLevelStacks,
    showOverflow: () -> Boolean,
    onChangeOverflow: (Boolean) -> Unit,
) {
    if (layoutType == NavigationSuiteType.NavigationBar) {
        TopLevelStackKey.entries.take(5)
            .forEachIndexed { index, key ->
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
        val remaining = TopLevelStackKey.entries.drop(5)
        if (remaining.isNotEmpty()) {
            item(
                icon = {
                    Box {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            stringResource(Res.string.alley_edit_top_level_nav_more),
                        )
                        if (showOverflow()) {
                            Popup(
                                popupPositionProvider = PlaceAbovePositionProvider,
                                onDismissRequest = { onChangeOverflow(false) },
                            ) {
                                Column(
                                    modifier = Modifier
                                        .width(IntrinsicSize.Min)
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    remaining.forEachIndexed { index, key ->
                                        Text(
                                            text = stringResource(key.title),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    navStack.moveToTopLevelStack(index + 5)
                                                    onChangeOverflow(false)
                                                }
                                                .padding(
                                                    horizontal = 16.dp,
                                                    vertical = 8.dp
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                label = { Text(stringResource(Res.string.alley_edit_top_level_nav_more)) },
                selected = false,
                onClick = { onChangeOverflow(!showOverflow()) },
                modifier = Modifier.zIndex(3f)
            )
        }
    } else {
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
            onClickEditImages = { requestKey, displayName, images ->
                navStack.navigate(
                    AlleyEditDestination.ImagesEdit(requestKey, images, displayName)
                )
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.ArtistCatalogs> { route ->
        ArtistCatalogsQueueScreen(
            dataYear = route.dataYear,
            graph = graph,
            onSelectEntry = {
                navStack.navigate(
                    AlleyEditDestination.ArtistEdit(
                        route.dataYear,
                        it.artistId,
                        it.link
                    )
                )
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.ArtistEdit> { route ->
        ArtistEditScreen(
            dataYear = route.dataYear,
            artistId = route.artistId,
            catalogLink = route.catalogLink,
            graph = graph,
            onClickBack = onClickBack,
            onClickEditImages = { requestKey, displayName, images ->
                navStack.navigate(
                    AlleyEditDestination.ImagesEdit(requestKey, images, displayName)
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
            onClickBackAndEditArtist = { artistId ->
                onClickBack(true)
                navStack.navigate(AlleyEditDestination.ArtistEdit(route.dataYear, artistId))
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.ArtistFormMerge> { route ->
        ArtistFormMergeScreen(
            dataYear = route.dataYear,
            artistId = route.artistId,
            graph = graph,
            onClickBack = onClickBack,
            onClickBackAndEditArtist = { artistId ->
                onClickBack(true)
                navStack.navigate(AlleyEditDestination.ArtistEdit(route.dataYear, artistId))
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.ArtistFormQueue> {
        ArtistFormQueueScreen(
            graph = graph,
            onSelectEntry = {
                // TODO: Support other conventions?
                navStack.navigate(
                    AlleyEditDestination.ArtistFormMerge(
                        dataYear = DataYear.LATEST,
                        artistId = it,
                    )
                )
            },
            onSelectHistoryEntry = { artistId, formTimestamp ->
                // TODO: Support other conventions?
                navStack.navigate(
                    AlleyEditDestination.ArtistFormHistory(
                        dataYear = DataYear.LATEST,
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
    sharedElementEntry<AlleyEditDestination.ImagesEdit> { route ->
        ImagesEditScreen(
            requestKey = route.requestKey,
            displayName = route.displayName,
            initialImages = route.images,
            onClickBack = onClickBack,
            viewModel = viewModel {
                graph.imagesEditViewModelFactory.create(
                    images = route.images,
                    savedStateHandle = createSavedStateHandle(),
                )
            },
        )
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
            booths = route.booths,
            link = route.link,
            graph = graph,
            onClickBack = onClickBack,
            onClickEditImages = { requestKey, displayName, images ->
                navStack.navigate(
                    AlleyEditDestination.ImagesEdit(requestKey, images, displayName)
                )
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.StampRallyEdit> { route ->
        StampRallyEditScreen(
            dataYear = route.dataYear,
            stampRallyId = route.stampRallyId,
            graph = graph,
            onClickBack = onClickBack,
            onClickEditImages = { requestKey, displayName, images ->
                navStack.navigate(
                    AlleyEditDestination.ImagesEdit(requestKey, images, displayName)
                )
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
    sharedElementEntry<AlleyEditDestination.StampRalliesQueue> { route ->
        StampRallyLinksQueueScreen(
            dataYear = route.dataYear,
            graph = graph,
            onSelectEntry = {
                navStack.navigate(
                    AlleyEditDestination.StampRallyAdd(
                        dataYear = route.dataYear,
                        booths = it.booths.toSet(),
                        link = it.link,
                    )
                )
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.StampRallyFormQueue> {
        StampRallyFormQueueScreen(
            graph = graph,
            onSelectEntry = { artistId, stampRallyId ->
                navStack.navigate(
                    AlleyEditDestination.StampRallyFormMerge(
                        dataYear = DataYear.LATEST,
                        artistId = artistId,
                        stampRallyId = stampRallyId,
                    )
                )
            },
            onSelectHistoryEntry = { artistId, stampRallyId, formTimestamp ->
                navStack.navigate(
                    AlleyEditDestination.StampRallyFormHistory(
                        dataYear = DataYear.LATEST,
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
            onClickBackAndEdit = { rallyId ->
                onClickBack(true)
                navStack.navigate(
                    AlleyEditDestination.StampRallyEdit(
                        route.dataYear,
                        rallyId.toString()
                    )
                )
            },
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
            onClickBackAndEdit = { rallyId ->
                onClickBack(true)
                navStack.navigate(
                    AlleyEditDestination.StampRallyEdit(
                        route.dataYear,
                        rallyId.toString()
                    )
                )
            },
        )
    }

    sharedElementEntry<AlleyEditDestination.RemoteArtistDataQueue> {
        RemoteArtistDataQueueScreen(
            graph = graph,
            onSelectEntry = {
                // TODO: Support other years?
                navStack.navigate(
                    AlleyEditDestination.RemoteArtistDataMerge(
                        dataYear = DataYear.LATEST,
                        id = it.id,
                    )
                )
            },
            onSelectHistoryEntry = {
                navStack.navigate(
                    AlleyEditDestination.RemoteArtistDataHistoryMerge(
                        dataYear = DataYear.LATEST,
                        id = it.id,
                        timestamp = it.timestamp,
                    )
                )
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.RemoteArtistDataMerge> { route ->
        RemoteArtistDataMergeScreen(
            dataYear = route.dataYear,
            id = route.id,
            graph = graph,
            onClickBack = onClickBack,
            onClickBackAndEditArtist = { artistId ->
                onClickBack(true)
                navStack.navigate(AlleyEditDestination.ArtistEdit(route.dataYear, artistId))
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.RemoteArtistDataHistoryMerge> {
        RemoteArtistDataHistoryMergeScreen(
            dataYear = it.dataYear,
            id = it.id,
            timestamp = it.timestamp,
            graph = graph,
            onClickBack = onClickBack,
        )
    }
}

private object PlaceAbovePositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
        val y = anchorBounds.top - popupContentSize.height
        return IntOffset(x, y)
    }

}
