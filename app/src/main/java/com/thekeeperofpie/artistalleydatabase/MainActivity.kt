package com.thekeeperofpie.artistalleydatabase

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import com.mxalbert.sharedelements.SharedElementsRoot
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.art.ArtNavDestinations
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseViewModel
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryNavigator
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils.navToEntryDetails
import com.thekeeperofpie.artistalleydatabase.export.ExportScreen
import com.thekeeperofpie.artistalleydatabase.export.ExportViewModel
import com.thekeeperofpie.artistalleydatabase.importing.ImportScreen
import com.thekeeperofpie.artistalleydatabase.importing.ImportViewModel
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems
import com.thekeeperofpie.artistalleydatabase.search.advanced.AdvancedSearchScreen
import com.thekeeperofpie.artistalleydatabase.search.advanced.AdvancedSearchViewModel
import com.thekeeperofpie.artistalleydatabase.search.results.SearchResultsScreen
import com.thekeeperofpie.artistalleydatabase.search.results.SearchResultsViewModel
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.settings.SettingsScreen
import com.thekeeperofpie.artistalleydatabase.settings.SettingsViewModel
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseSyncWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val STARTING_NAV_DESTINATION = "starting_nav_destination"
    }

    @Inject
    lateinit var scopedApplication: ScopedApplication

    @Inject
    lateinit var artEntryNavigator: ArtEntryNavigator

    @Inject
    lateinit var cdEntryNavigator: CdEntryNavigator

    @Inject
    lateinit var settings: SettingsProvider

    @Inject
    lateinit var aniListOAuthStore: AniListOAuthStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hideStatusBar = settings.hideStatusBar.value
        if (BuildConfig.DEBUG && hideStatusBar) {
            // TODO: On release, the lack of this prevents WindowInsets.isImeVisible from working
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        val startDestinationFromIntent = intent.getStringExtra(STARTING_NAV_DESTINATION)
        val startDestinationFromSettings = settings.navDrawerStartDestination.value
        val startDestination = (startDestinationFromIntent
            ?: startDestinationFromSettings)
            ?.let { startId -> NavDrawerItems.values().find { it.id == startId }?.id }
            ?: AnimeNavDestinations.HOME.id
        setContent {
            val navController = rememberNavController()
            ArtistAlleyDatabaseTheme(navController) {
                Surface {
                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val navDrawerItems = NavDrawerItems.values()

                    fun onClickNav() = scope.launch { drawerState.open() }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            // TODO: This is not a good way to to infer the selected root
                            var selectedRouteIndex by rememberSaveable {
                                mutableIntStateOf(
                                    NavDrawerItems.values()
                                        .indexOfFirst { it.id == startDestination })
                            }

                            StartDrawer(
                                navDrawerItems = { navDrawerItems },
                                selectedIndex = { selectedRouteIndex },
                                onSelectIndex = {
                                    if (selectedRouteIndex == it) {
                                        scope.launch { drawerState.close() }
                                    } else {
                                        selectedRouteIndex = it
                                        val newId = navDrawerItems[it].id
                                        navController.navigate(newId) {
                                            launchSingleTop = true
                                            restoreState = true
                                            val rootRoute = navController.currentBackStackEntry
                                                ?.destination?.route
                                            if (rootRoute != null) {
                                                popUpTo(rootRoute) {
                                                    inclusive = true
                                                    saveState = true
                                                }
                                            }
                                        }
                                        if (startDestinationFromIntent == null) {
                                            settings.navDrawerStartDestination.value = newId
                                        }
                                    }
                                },
                                onCloseDrawer = { scope.launch { drawerState.close() } },
                            )
                        }
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            val uriHandler = LocalUriHandler.current
                            val navigationCallback =
                                AnimeNavigator.NavigationCallback(
                                    navController,
                                    cdEntryNavigator,
                                    uriHandler::openUri,
                                )
                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                SharedElementsRoot {
                                    NavHost(
                                        navController = navController,
                                        startDestination = startDestination,
                                    ) {
                                        AnimeNavigator.initialize(
                                            navHostController = navController,
                                            navGraphBuilder = this,
                                            onClickNav = ::onClickNav,
                                            onClickAuth = {
                                                aniListOAuthStore.launchAuthRequest(
                                                    this@MainActivity
                                                )
                                            },
                                            onClickSettings = {
                                                navController.navigate(AppNavDestinations.SETTINGS.id)
                                            },
                                            navigationCallback = navigationCallback,
                                        )

                                        artEntryNavigator.initialize(
                                            ::onClickNav,
                                            navController,
                                            this
                                        )
                                        cdEntryNavigator.initialize(
                                            ::onClickNav,
                                            navController,
                                            this
                                        )

                                        composable(AppNavDestinations.BROWSE.id) {
                                            val viewModel = hiltViewModel<BrowseViewModel>()
                                            BrowseScreen(
                                                onClickNav = ::onClickNav,
                                                tabs = viewModel.tabs,
                                                onClick = { tabContent, entry ->
                                                    viewModel.onSelectEntry(
                                                        navController,
                                                        tabContent,
                                                        entry
                                                    )
                                                },
                                                onPageRequested = viewModel::onPageRequested,
                                            )
                                        }

                                        composable(AppNavDestinations.SEARCH.id) {
                                            val viewModel = hiltViewModel<AdvancedSearchViewModel>()
                                            AdvancedSearchScreen(
                                                onClickNav = ::onClickNav,
                                                loading = { false },
                                                sections = { viewModel.sections },
                                                onClickClear = viewModel::onClickClear,
                                                onClickSearch = {
                                                    val queryId = viewModel.onClickSearch()
                                                    navController.navigate(
                                                        AppNavDestinations.SEARCH_RESULTS.id +
                                                                "?queryId=$queryId"
                                                    )
                                                },
                                            )
                                        }

                                        composable(AppNavDestinations.IMPORT.id) {
                                            val viewModel = hiltViewModel<ImportViewModel>()
                                            ImportScreen(
                                                onClickNav = ::onClickNav,
                                                uriString = viewModel.importUriString.orEmpty(),
                                                onUriStringEdit = {
                                                    viewModel.importUriString = it
                                                },
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

                                        composable(AppNavDestinations.EXPORT.id) {
                                            val viewModel = hiltViewModel<ExportViewModel>()
                                            ExportScreen(
                                                onClickNav = ::onClickNav,
                                                uriString = { viewModel.exportUriString.orEmpty() },
                                                onUriStringEdit = {
                                                    viewModel.exportUriString = it
                                                },
                                                onContentUriSelected = {
                                                    viewModel.exportUriString = it?.toString()
                                                },
                                                onClickExport = viewModel::onClickExport,
                                                exportProgress = { viewModel.exportProgress },
                                                errorRes = { viewModel.errorResource },
                                                onErrorDismiss = { viewModel.errorResource = null }
                                            )
                                        }

                                        composable(
                                            route = AppNavDestinations.SEARCH_RESULTS.id +
                                                    "?queryId={queryId}",
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
                                                            route = ArtNavDestinations.ENTRY_DETAILS.id,
                                                            listOf(entry.id.valueId)
                                                        )
                                                    }
                                                },
                                                onLongClickEntry = viewModel::selectEntry,
                                                onClickClear = viewModel::clearSelected,
                                                onClickEdit = {
                                                    navController.navToEntryDetails(
                                                        ArtNavDestinations.ENTRY_DETAILS.id,
                                                        viewModel.selectedEntries.values.map { it.id.valueId }
                                                    )
                                                },
                                                onConfirmDelete = viewModel::onDeleteSelected,
                                            )
                                        }

                                        composable(AppNavDestinations.SETTINGS.id) {
                                            val viewModel =
                                                hiltViewModel<SettingsViewModel>().apply {
                                                    initialize(
                                                        onClickDatabaseFetch = {
                                                            val request =
                                                                OneTimeWorkRequestBuilder<DatabaseSyncWorker>()
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
                                                onClickNav = ::onClickNav,
                                                onClickAniListClear = viewModel::clearAniListCache,
                                                onClickVgmdbClear = viewModel::clearVgmdbCache,
                                                onClickDatabaseFetch = viewModel::onClickDatabaseFetch,
                                                onClickClearDatabaseById = viewModel::onClickClearDatabaseById,
                                                onClickRebuildDatabase = viewModel::onClickRebuildDatabase,
                                                onClickCropClear = viewModel::onClickCropClear,
                                                onClickClearAniListOAuth = viewModel::onClickClearAniListOAuth,
                                                networkLoggingLevel = { viewModel.networkLoggingLevel.collectAsState().value },
                                                onChangeNetworkLoggingLevel = viewModel::onChangeNetworkLoggingLevel,
                                                hideStatusBar = { viewModel.hideStatusBar.collectAsState().value },
                                                onHideStatusBarChanged = viewModel::onHideStatusBarChanged,
                                                onClickShowLastCrash = {
                                                    navController.navigate(AppNavDestinations.CRASH.id)
                                                }
                                            )
                                        }

                                        composable(
                                            route = AppNavDestinations.CRASH.id,
                                            deepLinks = listOf(
                                                navDeepLink {
                                                    action =
                                                        scopedApplication.mainActivityInternalAction
                                                    uriPattern =
                                                        "${scopedApplication.app.packageName}:///${AppNavDestinations.CRASH.id}"
                                                }
                                            )
                                        ) {
                                            SideEffect { settings.lastCrashShown.value = true }
                                            CrashScreen(
                                                settings = settings,
                                                onClickBack = { navController.popBackStack() },
                                            )
                                        }
                                    }
                                }

                                // Ignore touch events to allow drawer swipe to work
                                Box(
                                    Modifier
                                        .fillMaxHeight()
                                        .width(16.dp)
                                        .align(Alignment.CenterStart)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {},
                                        )
                                )
                            }

                            if (BuildConfig.DEBUG) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary)
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.debug_variant),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hideStatusBar) {
            window.insetsController?.run {
                hide(WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    @Composable
    private fun StartDrawer(
        navDrawerItems: () -> Array<NavDrawerItems>,
        selectedIndex: () -> Int,
        onSelectIndex: (Int) -> Unit,
        onCloseDrawer: () -> Unit,
    ) {
        ModalDrawerSheet {
            Spacer(modifier = Modifier.height(16.dp))
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
}
