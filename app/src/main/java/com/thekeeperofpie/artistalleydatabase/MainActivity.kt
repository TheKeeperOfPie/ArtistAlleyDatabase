package com.thekeeperofpie.artistalleydatabase

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.mxalbert.sharedelements.SharedElementsRoot
import com.thekeeperofpie.artistalleydatabase.anime.AnimeHomeScreen
import com.thekeeperofpie.artistalleydatabase.anime.AnimeHomeViewModel
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.art.search.ArtSearchViewModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseViewModel
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryNavigator
import com.thekeeperofpie.artistalleydatabase.cds.search.CdSearchViewModel
import com.thekeeperofpie.artistalleydatabase.compose.LazyStaggeredGrid
import com.thekeeperofpie.artistalleydatabase.entry.EntryNavigator
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils.navToEntryDetails
import com.thekeeperofpie.artistalleydatabase.export.ExportScreen
import com.thekeeperofpie.artistalleydatabase.export.ExportViewModel
import com.thekeeperofpie.artistalleydatabase.home.HomeScreen
import com.thekeeperofpie.artistalleydatabase.importing.ImportScreen
import com.thekeeperofpie.artistalleydatabase.importing.ImportViewModel
import com.thekeeperofpie.artistalleydatabase.navigation.NavDestinations
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems
import com.thekeeperofpie.artistalleydatabase.search.advanced.AdvancedSearchScreen
import com.thekeeperofpie.artistalleydatabase.search.advanced.AdvancedSearchViewModel
import com.thekeeperofpie.artistalleydatabase.search.results.SearchResultsScreen
import com.thekeeperofpie.artistalleydatabase.search.results.SearchResultsViewModel
import com.thekeeperofpie.artistalleydatabase.settings.SettingsScreen
import com.thekeeperofpie.artistalleydatabase.settings.SettingsViewModel
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseSyncWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.accompanist.navigation.animation.composable as animationComposable

@OptIn(ExperimentalAnimationApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val STARTING_NAV_DESTINATION = "starting_nav_destination"
    }

    @Inject
    lateinit var entryNavigators: Set<@JvmSuppressWildcards EntryNavigator>

    @Inject
    lateinit var artEntryNavigator: ArtEntryNavigator

    @Inject
    lateinit var cdEntryNavigator: CdEntryNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        setContent {
            ArtistAlleyDatabaseTheme {
                Surface {
                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val navDrawerItems = NavDrawerItems.items()
                    val defaultSelectedItemIndex = intent.getStringExtra(STARTING_NAV_DESTINATION)
                        ?.let { navId -> navDrawerItems.indexOfFirst { it.id == navId } }
                        ?: NavDrawerItems.INITIAL_INDEX

                    fun onClickNav() = scope.launch { drawerState.open() }
                    var selectedItemIndex by rememberSaveable {
                        mutableStateOf(defaultSelectedItemIndex)
                    }
                    val selectedItem = navDrawerItems[selectedItemIndex]

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            StartDrawer(
                                navDrawerItems = { navDrawerItems },
                                selectedIndex = { selectedItemIndex },
                                onSelectIndex = { selectedItemIndex = it },
                                onCloseDrawer = { scope.launch { drawerState.close() } },
                            )
                        },
                        content = {
                            val block = @Composable {
                                when (selectedItem) {
                                    NavDrawerItems.Anime -> AnimeScreen(
                                        onClickNav = ::onClickNav,
                                    )
                                    NavDrawerItems.Art -> ArtScreen(::onClickNav)
                                    NavDrawerItems.Cds -> CdsScreen(::onClickNav)
                                    NavDrawerItems.Browse -> BrowseScreen(::onClickNav)
                                    NavDrawerItems.Search -> SearchScreen(::onClickNav)
                                    NavDrawerItems.Import -> {
                                        val viewModel = hiltViewModel<ImportViewModel>()
                                        ImportScreen(
                                            onClickNav = ::onClickNav,
                                            uriString = viewModel.importUriString.orEmpty(),
                                            onUriStringEdit = { viewModel.importUriString = it },
                                            onContentUriSelected = {
                                                viewModel.importUriString = it?.toString()
                                            },
                                            dryRun = { viewModel.dryRun },
                                            onToggleDryRun = {
                                                viewModel.dryRun = !viewModel.dryRun
                                            },
                                            replaceAll = { viewModel.replaceAll },
                                            onToggleReplaceAll = {
                                                viewModel.replaceAll = !viewModel.replaceAll
                                            },
                                            syncAfter = { viewModel.syncAfter },
                                            onToggleSyncAfter = {
                                                viewModel.syncAfter = !viewModel.syncAfter
                                            },
                                            onClickImport = viewModel::onClickImport,
                                            importProgress = { viewModel.importProgress },
                                            errorRes = { viewModel.errorResource },
                                            onErrorDismiss = { viewModel.errorResource = null }
                                        )
                                    }
                                    NavDrawerItems.Export -> {
                                        val viewModel = hiltViewModel<ExportViewModel>()
                                        ExportScreen(
                                            onClickNav = ::onClickNav,
                                            uriString = { viewModel.exportUriString.orEmpty() },
                                            onUriStringEdit = { viewModel.exportUriString = it },
                                            onContentUriSelected = {
                                                viewModel.exportUriString = it?.toString()
                                            },
                                            onClickExport = viewModel::onClickExport,
                                            exportProgress = { viewModel.exportProgress },
                                            errorRes = { viewModel.errorResource },
                                            onErrorDismiss = { viewModel.errorResource = null }
                                        )
                                    }
                                    NavDrawerItems.Settings -> SettingsScreen(::onClickNav)
                                }.run { /* exhaust */ }
                            }

                            if (BuildConfig.DEBUG) {
                                Column {
                                    Box(modifier = Modifier.weight(1f)) {
                                        block()
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(colorResource(R.color.launcher_background))
                                            .fillMaxWidth()
                                            .height(56.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.debug_variant),
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            } else {
                                block()
                            }
                        }
                    )
                }
            }
        }

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.run {
                hide(WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    @Composable
    private fun StartDrawer(
        navDrawerItems: () -> List<NavDrawerItems>,
        selectedIndex: () -> Int,
        onSelectIndex: (Int) -> Unit,
        onCloseDrawer: () -> Unit,
    ) {
        ModalDrawerSheet {
            navDrawerItems().forEachIndexed { index, item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = { Text(stringResource(item.titleRes)) },
                    selected = index == selectedIndex(),
                    onClick = {
                        onCloseDrawer()
                        onSelectIndex(index)
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }

    @Composable
    private fun AnimeScreen(
        onClickNav: () -> Unit,
    ) {
        val navController = rememberAnimatedNavController()
        SharedElementsRoot {
            AnimatedNavHost(
                navController = navController,
                startDestination = NavDestinations.HOME
            ) {
                animationComposable(
                    NavDestinations.HOME,
                    enterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down
                        )
                    },
                ) {
                    val viewModel = hiltViewModel<AnimeHomeViewModel>()
                    AnimeHomeScreen(
                        onClickNav = onClickNav,
                        needAuth = { viewModel.needAuth },
                        onClickAuth = { viewModel.onClickAuth(this@MainActivity) },
                        onSubmitAuthToken = viewModel::onSubmitAuthToken,
                        onTagClick = { tagId, tagName ->
                            AnimeNavigator.onTagClick(navController, tagId, tagName)
                        },
                        onMediaClick = { AnimeNavigator.onMediaClick(navController, it) },
                    )
                }

                AnimeNavigator.initialize(navController, this, onClickNav)
            }
        }
    }

    @Composable
    private fun ArtScreen(onClickNav: () -> Unit) {
        val navController = rememberNavController()
        SharedElementsRoot {
            NavHost(navController = navController, startDestination = NavDestinations.HOME) {
                composable(NavDestinations.HOME) {
                    val viewModel = hiltViewModel<ArtSearchViewModel>()
                    val lazyStaggeredGridState =
                        LazyStaggeredGrid.rememberLazyStaggeredGridState(columnCount = 2)
                    HomeScreen(
                        onClickNav = onClickNav,
                        query = { viewModel.query.collectAsState().value?.query.orEmpty() },
                        onQueryChange = viewModel::onQuery,
                        options = { viewModel.options },
                        onOptionChanged = { viewModel.refreshQuery() },
                        entries = { viewModel.results.collectAsLazyPagingItems() },
                        selectedItems = { viewModel.selectedEntries.keys },
                        onClickAddFab = {
                            navController.navToEntryDetails(route = "artEntryDetails", emptyList())
                        },
                        onClickEntry = { index, entry ->
                            if (viewModel.selectedEntries.isNotEmpty()) {
                                viewModel.selectEntry(index, entry)
                            } else {
                                navController.navToEntryDetails(
                                    route = "artEntryDetails",
                                    listOf(entry.id.valueId)
                                )
                            }
                        },
                        onLongClickEntry = viewModel::selectEntry,
                        onClickClear = viewModel::clearSelected,
                        onClickEdit = {
                            navController.navToEntryDetails(
                                "artEntryDetails",
                                viewModel.selectedEntries.values.map { it.id.valueId }
                            )
                        },
                        onConfirmDelete = viewModel::deleteSelected,
                        lazyStaggeredGridState = lazyStaggeredGridState,
                    )
                }

                artEntryNavigator.initialize(navController, this)
            }
        }
    }

    @Composable
    private fun CdsScreen(onClickNav: () -> Unit) {
        val navController = rememberNavController()
        SharedElementsRoot {
            NavHost(navController = navController, startDestination = NavDestinations.HOME) {
                composable(NavDestinations.HOME) {
                    val viewModel = hiltViewModel<CdSearchViewModel>()
                    val lazyStaggeredGridState =
                        LazyStaggeredGrid.rememberLazyStaggeredGridState(columnCount = 2)
                    HomeScreen(
                        onClickNav = onClickNav,
                        query = { viewModel.query.collectAsState().value?.query.orEmpty() },
                        onQueryChange = viewModel::onQuery,
                        options = { viewModel.options },
                        onOptionChanged = { viewModel.refreshQuery() },
                        entries = { viewModel.results.collectAsLazyPagingItems() },
                        selectedItems = { viewModel.selectedEntries.keys },
                        onClickAddFab = {
                            navController.navToEntryDetails(route = "cdEntryDetails", emptyList())
                        },
                        onClickEntry = { index, entry ->
                            if (viewModel.selectedEntries.isNotEmpty()) {
                                viewModel.selectEntry(index, entry)
                            } else {
                                navController.navToEntryDetails(
                                    route = "cdEntryDetails",
                                    listOf(entry.id.valueId)
                                )
                            }
                        },
                        onLongClickEntry = viewModel::selectEntry,
                        onClickClear = viewModel::clearSelected,
                        onClickEdit = {
                            navController.navToEntryDetails(
                                "cdEntryDetails",
                                viewModel.selectedEntries.values.map { it.id.valueId }
                            )
                        },
                        onConfirmDelete = viewModel::deleteSelected,
                        lazyStaggeredGridState = lazyStaggeredGridState,
                    )
                }

                cdEntryNavigator.initialize(navController, this)
            }
        }
    }

    @Composable
    private fun BrowseScreen(onClickNav: () -> Unit) {
        val navController = rememberNavController()
        SharedElementsRoot {
            NavHost(navController = navController, startDestination = "browse") {
                composable("browse") {
                    val viewModel = hiltViewModel<BrowseViewModel>()
                    BrowseScreen(
                        onClickNav = onClickNav,
                        tabs = viewModel.tabs,
                        onClick = { tabContent, entry ->
                            viewModel.onSelectEntry(navController, tabContent, entry)
                        },
                        onPageRequested = viewModel::onPageRequested,
                    )
                }

                entryNavigators.forEach { it.initialize(navController, this) }
            }
        }
    }

    @Composable
    private fun SearchScreen(onClickNav: () -> Unit) {
        val navController = rememberNavController()
        SharedElementsRoot {
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    val viewModel = hiltViewModel<AdvancedSearchViewModel>()
                    AdvancedSearchScreen(
                        onClickNav = onClickNav,
                        loading = { false },
                        sections = { viewModel.sections },
                        onClickClear = viewModel::onClickClear,
                        onClickSearch = {
                            val queryId = viewModel.onClickSearch()
                            navController.navigate("results?queryId=$queryId")
                        },
                    )
                }

                composable(
                    "results?queryId={queryId}",
                    arguments = listOf(
                        navArgument("queryId") {
                            type = NavType.StringType
                            nullable = false
                        },
                    )
                ) {
                    val arguments = it.arguments!!
                    val queryId = arguments.getString("queryId")!!
                    val viewModel = hiltViewModel<SearchResultsViewModel>()
                    viewModel.initialize(queryId)
                    SearchResultsScreen(
                        onClickBack = it.destination.parent?.let {
                            { navController.popBackStack() }
                        },
                        loading = { viewModel.loading },
                        entries = { viewModel.entries.collectAsLazyPagingItems() },
                        selectedItems = { viewModel.selectedEntries.keys },
                        onClickEntry = { index, entry ->
                            if (viewModel.selectedEntries.isNotEmpty()) {
                                viewModel.selectEntry(index, entry)
                            } else {
                                navController.navToEntryDetails(
                                    route = "artEntryDetails",
                                    listOf(entry.id.valueId)
                                )
                            }
                        },
                        onLongClickEntry = viewModel::selectEntry,
                        onClickClear = viewModel::clearSelected,
                        onClickEdit = {
                            navController.navToEntryDetails(
                                "artEntryDetails",
                                viewModel.selectedEntries.values.map { it.id.valueId }
                            )
                        },
                        onConfirmDelete = viewModel::onDeleteSelected,
                    )
                }

                entryNavigators.forEach { it.initialize(navController, this) }
            }
        }
    }

    @Composable
    private fun SettingsScreen(onClickNav: () -> Unit) {
        val viewModel = hiltViewModel<SettingsViewModel>().apply {
            initialize(
                onClickDatabaseFetch = {
                    val request = OneTimeWorkRequestBuilder<DatabaseSyncWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build()

                    it.enqueueUniqueWork(
                        DatabaseSyncWorker.UNIQUE_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        request
                    )
                }
            )
        }
        SettingsScreen(
            onClickNav = onClickNav,
            onClickAniListClear = viewModel::clearAniListCache,
            onClickVgmdbClear = viewModel::clearVgmdbCache,
            onClickDatabaseFetch = viewModel::onClickDatabaseFetch,
            onClickClearDatabaseById = viewModel::onClickClearDatabaseById,
            onClickRebuildDatabase = viewModel::onClickRebuildDatabase,
            onClickCropClear = viewModel::onClickCropClear,
            onClickClearAniListOAuth = viewModel::onClickClearAniListOAuth,
            networkLoggingLevel = { viewModel.networkLoggingLevel.collectAsState().value },
            onChangeNetworkLoggingLevel = viewModel::onChangeNetworkLoggingLevel
        )
    }
}
