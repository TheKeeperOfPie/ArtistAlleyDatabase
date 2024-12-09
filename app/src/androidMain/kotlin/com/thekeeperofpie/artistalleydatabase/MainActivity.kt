package com.thekeeperofpie.artistalleydatabase

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import artistalleydatabase.app.generated.resources.Res
import artistalleydatabase.app.generated.resources.crash_share_chooser_title
import artistalleydatabase.app.generated.resources.nav_drawer_manga
import com.anilist.data.type.MediaType
import com.thekeeperofpie.anichive.BuildConfig
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionCharacters
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionStaff
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionVoiceActor
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGenrePreview
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaTagPreview
import com.thekeeperofpie.artistalleydatabase.anime2anime.Anime2AnimeScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.export.ExportScreen
import com.thekeeperofpie.artistalleydatabase.importing.ImportScreen
import com.thekeeperofpie.artistalleydatabase.markdown.LocalMarkdown
import com.thekeeperofpie.artistalleydatabase.monetization.LocalMonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.LocalSubscriptionProvider
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems
import com.thekeeperofpie.artistalleydatabase.settings.SettingsScreen
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme
import com.thekeeperofpie.artistalleydatabase.utils.ComponentProvider
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseSyncWorker
import com.thekeeperofpie.artistalleydatabase.utils_compose.CrashScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.FullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalAppUpdateChecker
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalComposeSettings
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalShareHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.LocalImageColorsState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberImageColorsState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberNavigationController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.enums.EnumEntries

@OptIn(ExperimentalSharedTransitionApi::class)
class MainActivity : ComponentActivity() {

    companion object {
        const val STARTING_NAV_DESTINATION = "starting_nav_destination"
    }

    private val applicationComponent by lazy {
        (applicationContext as ComponentProvider).singletonComponent<ApplicationComponent>()
    }

    private val appMetadataProvider by lazy { applicationComponent.appMetadataProvider }
    private val artEntryNavigator by lazy { applicationComponent.artEntryNavigator }
    private val cdEntryNavigator by lazy { applicationComponent.cdEntryNavigator }
    private val featureOverrideProvider by lazy { applicationComponent.featureOverrideProvider }
    private val ignoreController by lazy { applicationComponent.ignoreController }
    private val markdown by lazy { applicationComponent.markdown }
    private val mediaGenreDialogController by lazy { applicationComponent.mediaGenreDialogController }
    private val mediaTagDialogController by lazy { applicationComponent.mediaTagDialogController }
    private val monetizationController by lazy { applicationComponent.monetizationController }
    private val navigationTypeMap by lazy { applicationComponent.navigationTypeMap }
    private val notificationsController by lazy { applicationComponent.notificationsController }
    private val platformOAuthStore by lazy { applicationComponent.platformOAuthStore }
    private val settings by lazy { applicationComponent.settingsProvider }
    private val workManager by lazy { applicationComponent.workManager }

    private val activityComponent by lazy {
        ActivityComponent::class.create(applicationComponent, this)
    }
    private val appUpdateChecker by lazy { activityComponent.injector.appUpdateChecker }
    private val monetizationProvider by lazy { activityComponent.injector.monetizationProvider }
    private val subscriptionProvider by lazy { activityComponent.injector.subscriptionProvider }
    private val shareHandler by lazy { activityComponent.injector.shareHandler }

    private val fullScreenImageHandler = FullscreenImageHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hideStatusBar = BuildConfig.DEBUG && settings.hideStatusBar.value
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val startDestinationFromIntent = intent.getStringExtra(STARTING_NAV_DESTINATION)
        val startDestinationFromSettings = settings.navDrawerStartDestination.value
            .takeUnless { it == NavDrawerItems.SETTINGS.id }
        val startDestination = (startDestinationFromIntent
            ?: startDestinationFromSettings)
            ?.let { startId -> NavDrawerItems.values().find { it.id == startId }?.id }
            ?: NavDrawerItems.ANIME.id

        setContent {
            val navHostController = rememberNavController()
            val languageOptionMedia by settings.languageOptionMedia.collectAsState()
            val languageOptionCharacters by settings.languageOptionCharacters.collectAsState()
            val languageOptionStaff by settings.languageOptionStaff.collectAsState()
            val languageOptionVoiceActor by settings.languageOptionVoiceActor.collectAsState()
            val showFallbackVoiceActor by settings.showFallbackVoiceActor.collectAsState()

            ArtistAlleyDatabaseTheme(settings = settings, navHostController = navHostController) {
                val imageColorsState = rememberImageColorsState()
                val navigationController = rememberNavigationController(navHostController)
                CompositionLocalProvider(
                    LocalNavigationController provides navigationController,
                    LocalMonetizationProvider provides monetizationProvider,
                    LocalSubscriptionProvider provides subscriptionProvider,
                    LocalMediaTagDialogController provides mediaTagDialogController,
                    LocalMediaGenreDialogController provides mediaGenreDialogController,
                    LocalAppUpdateChecker provides appUpdateChecker,
                    LocalLanguageOptionMedia provides languageOptionMedia,
                    LocalLanguageOptionCharacters provides languageOptionCharacters,
                    LocalLanguageOptionStaff provides languageOptionStaff,
                    LocalLanguageOptionVoiceActor provides
                            (languageOptionVoiceActor to showFallbackVoiceActor),
                    LocalFullscreenImageHandler provides fullScreenImageHandler,
                    LocalMarkdown provides markdown,
                    LocalComposeSettings provides settings.composeSettingsData(),
                    LocalImageColorsState provides imageColorsState,
                    LocalIgnoreController provides ignoreController,
                    LocalAnimeComponent provides applicationComponent,
                    LocalShareHandler provides shareHandler,
                ) {
                    // TODO: Draw inside insets for applicable screens
                    Surface(modifier = Modifier.safeDrawingPadding()) {
                        val startDrawerState = rememberDrawerState(DrawerValue.Closed)
                        val endDrawerState = rememberDrawerState(DrawerValue.Closed)
                        val scope = rememberCoroutineScope()
                        val navDrawerItems = NavDrawerItems.entries

                        fun onClickNav() = scope.launch { startDrawerState.open() }
                        val unlockDatabaseFeatures by monetizationController.unlockDatabaseFeatures
                            .collectAsState(false)

                        if (unlockDatabaseFeatures) {
                            var gesturesEnabled by remember { mutableStateOf(true) }
                            @Suppress("KotlinConstantConditions")
                            if (BuildConfig.BUILD_TYPE == "release") {
                                DisposableEffect(navHostController) {
                                    val listener =
                                        NavController.OnDestinationChangedListener { controller, destination, arguments ->
                                            gesturesEnabled = NavDrawerItems.entries
                                                .any { it.route == destination.route }
                                        }
                                    navHostController.addOnDestinationChangedListener(listener)
                                    onDispose {
                                        navHostController.removeOnDestinationChangedListener(
                                            listener
                                        )
                                    }
                                }
                            }
                            DebugDoubleDrawer(
                                applicationComponent = applicationComponent,
                                startDrawerState = startDrawerState,
                                endDrawerState = endDrawerState,
                                gesturesEnabled = gesturesEnabled,
                                drawerContent = {
                                    // TODO: This is not a good way to to infer the selected root
                                    var selectedRouteIndex by rememberSaveable {
                                        mutableIntStateOf(
                                            NavDrawerItems.entries
                                                .indexOfFirst { it.id == startDestination })
                                    }

                                    StartDrawer(
                                        drawerState = startDrawerState,
                                        navDrawerItems = { navDrawerItems },
                                        selectedIndex = { selectedRouteIndex },
                                        onSelectIndex = {
                                            if (selectedRouteIndex == it) {
                                                scope.launch { startDrawerState.close() }
                                            } else {
                                                selectedRouteIndex = it
                                                val navDrawerItem = navDrawerItems[it]
                                                navHostController.navigate(navDrawerItem.route) {
                                                    launchSingleTop = true
                                                    restoreState = true
                                                    val rootRoute =
                                                        navHostController.currentBackStackEntry
                                                            ?.destination?.route
                                                    if (rootRoute != null) {
                                                        popUpTo(rootRoute) {
                                                            inclusive = true
                                                            saveState = true
                                                        }
                                                    }
                                                }
                                                if (startDestinationFromIntent == null
                                                    && navDrawerItem != NavDrawerItems.SETTINGS
                                                ) {
                                                    settings.navDrawerStartDestination.value =
                                                        navDrawerItem.id
                                                }
                                            }
                                        },
                                        onCloseDrawer = { scope.launch { startDrawerState.close() } },
                                    )
                                }
                            ) {
                                Content(
                                    navigationController = navigationController,
                                    navHostController = navHostController,
                                    unlockDatabaseFeatures = true,
                                    onClickNav = ::onClickNav,
                                    startDestination = startDestination,
                                    onVariantBannerClick = {
                                        scope.launch { endDrawerState.open() }
                                    },
                                )
                            }
                        } else {
                            Content(
                                navigationController = navigationController,
                                navHostController = navHostController,
                                unlockDatabaseFeatures = false,
                                onClickNav = ::onClickNav,
                                startDestination = startDestination,
                                onVariantBannerClick = { scope.launch { endDrawerState.open() } },
                            )
                        }
                    }

                    val tagShown = mediaTagDialogController.tagShown
                    if (tagShown != null) {
                        MediaTagPreview(tag = tagShown) {
                            mediaTagDialogController.tagShown = null
                        }
                    }

                    val genreShown = mediaGenreDialogController.genreShown
                    if (genreShown != null) {
                        MediaGenrePreview(genre = genreShown) {
                            mediaGenreDialogController.genreShown = null
                        }
                    }

                    fullScreenImageHandler.ImageDialog()
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
        drawerState: DrawerState,
        navDrawerItems: () -> EnumEntries<NavDrawerItems>,
        selectedIndex: () -> Int,
        onSelectIndex: (Int) -> Unit,
        onCloseDrawer: () -> Unit,
    ) {
        ModalDrawerSheet(drawerState = drawerState) {
            Spacer(modifier = Modifier.height(16.dp))
            val preferredMediaType by settings.preferredMediaType.collectAsState()
            navDrawerItems().forEachIndexed { index, item ->
                NavigationDrawerItem(
                    icon = {
                        if (item == NavDrawerItems.ANIME && preferredMediaType == MediaType.MANGA) {
                            Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null)
                        } else {
                            Icon(item.icon, contentDescription = null)
                        }
                    },
                    label = {
                        if (item == NavDrawerItems.ANIME && preferredMediaType == MediaType.MANGA) {
                            Text(stringResource(Res.string.nav_drawer_manga))
                        } else {
                            Text(stringResource(item.titleRes))
                        }
                    },
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
    private fun Content(
        navigationController: NavigationController,
        navHostController: NavHostController,
        unlockDatabaseFeatures: Boolean,
        onClickNav: () -> Unit,
        startDestination: String,
        onVariantBannerClick: () -> Unit,
    ) {
        val navDrawerUpIconOption =
            UpIconOption.NavDrawer(onClickNav).takeIf { unlockDatabaseFeatures }
        Column(modifier = Modifier.fillMaxSize()) {
            val adsEnabled by monetizationController.adsEnabled.collectAsState(false)
            val subscribed by monetizationController.subscribed.collectAsState(false)
            // TODO: Offer option to still show ads even if subscribed?
            if (adsEnabled && !subscribed) {
                Column {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(82.dp)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(vertical = 16.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        LocalMonetizationProvider.current?.BannerAdView()
                    }
                    HorizontalDivider()
                }
            }
            Box(
                modifier = Modifier.weight(1f)
            ) {
                SharedTransitionLayout {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        NavHost(
                            navController = navHostController,
                            startDestination = startDestination,
                        ) {
                            applicationComponent.navDestinationProviders.forEach {
                                it.composable(this, navigationTypeMap)
                            }

                            AnimeNavigator.initialize(
                                navigationController = navigationController,
                                navGraphBuilder = this,
                                upIconOption = navDrawerUpIconOption,
                                navigationTypeMap = navigationTypeMap,
                                onClickAuth = {
                                    platformOAuthStore.launchAuthRequest(this@MainActivity)
                                },
                                onClickSettings = {
                                    navHostController.navigate(AppNavDestinations.SETTINGS.id)
                                },
                                onClickShowLastCrash = {
                                    navHostController.navigate(AppNavDestinations.CRASH.id)
                                },
                                component = applicationComponent,
                                cdEntryComponent = applicationComponent,
                                onCdEntryClick = { entryIds, imageCornerDp ->
                                    cdEntryNavigator
                                        .onCdEntryClick(navHostController, entryIds, imageCornerDp)
                                }
                            )

                            artEntryNavigator.initialize(
                                onClickNav = onClickNav,
                                navigationController = navigationController,
                                navHostController = navHostController,
                                navGraphBuilder = this,
                                artEntryComponent = applicationComponent,
                            )
                            cdEntryNavigator.initialize(
                                onClickNav = onClickNav,
                                navHostController = navHostController,
                                navGraphBuilder = this,
                                cdEntryComponent = applicationComponent,
                            )

                            sharedElementComposable(AppNavDestinations.BROWSE.id) {
                                val viewModel = viewModel { applicationComponent.browseViewModel() }
                                BrowseScreen(
                                    upIconOption = navDrawerUpIconOption,
                                    tabs = viewModel.tabs,
                                    onClick = { tabContent, entry ->
                                        viewModel.onSelectEntry(
                                            navHostController,
                                            tabContent,
                                            entry
                                        )
                                    },
                                    onPageRequested = viewModel::onPageRequested,
                                )
                            }

                            sharedElementComposable(AppNavDestinations.IMPORT.id) {
                                ImportScreen(
                                    viewModel = viewModel { applicationComponent.importViewModel() },
                                    upIconOption = navDrawerUpIconOption,
                                )
                            }

                            sharedElementComposable(AppNavDestinations.EXPORT.id) {
                                ExportScreen(
                                    viewModel = viewModel { applicationComponent.exportViewModel() },
                                    upIconOption = navDrawerUpIconOption,
                                )
                            }

                            sharedElementComposable(
                                route = "${AppNavDestinations.SETTINGS.id}?root={root}",
                                arguments = listOf(
                                    navArgument("root") {
                                        type = NavType.StringType
                                        nullable = true
                                    },
                                )
                            ) {
                                val viewModel =
                                    viewModel { applicationComponent.settingsViewModel() }
                                LaunchedEffect(viewModel) {
                                    viewModel.onClickDatabaseFetch.collectLatest {
                                        val request =
                                            OneTimeWorkRequestBuilder<DatabaseSyncWorker>()
                                                .setExpedited(
                                                    OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
                                                )
                                                .build()

                                        workManager.enqueueUniqueWork(
                                            DatabaseSyncWorker.UNIQUE_WORK_NAME,
                                            ExistingWorkPolicy.REPLACE,
                                            request
                                        )
                                    }
                                }
                                val root = it.arguments?.getString("root")
                                    ?.toBooleanStrictOrNull() == true
                                val navigationController = LocalNavigationController.current
                                SettingsScreen(
                                    viewModel = viewModel,
                                    appMetadataProvider = appMetadataProvider,
                                    upIconOption = navDrawerUpIconOption.takeIf { root }
                                        ?: UpIconOption.Back(navigationController),
                                    onClickShowLastCrash = {
                                        navHostController.navigate(AppNavDestinations.CRASH.id)
                                    },
                                    onClickFeatureTiers = {
                                        navigationController.navigate(AnimeDestination.FeatureTiers)
                                    },
                                    onClickViewMediaHistory = {
                                        navigationController.navigate(
                                            AnimeDestination.MediaHistory(mediaType = null)
                                        )
                                    },
                                    onClickViewMediaIgnore = {
                                        navigationController.navigate(
                                            AnimeDestination.Ignored(mediaType = null)
                                        )
                                    },
                                )
                            }

                            sharedElementComposable(AppNavDestinations.ANIME_2_ANIME.id) {
                                Anime2AnimeScreen(
                                    viewModel = viewModel { applicationComponent.anime2AnimeViewModel() },
                                    upIconOption = navDrawerUpIconOption,
                                )
                            }

                            sharedElementComposable(
                                route = AppNavDestinations.CRASH.id,
                                deepLinks = listOf(
                                    navDeepLink {
                                        action = "${application.packageName}.INTERNAL"
                                        uriPattern =
                                            "$packageName:///${AppNavDestinations.CRASH.id}"
                                    }
                                )
                            ) {
                                SideEffect { settings.lastCrashShown.value = true }
                                val shareTitle =
                                    stringResource(Res.string.crash_share_chooser_title)
                                CrashScreen(
                                    crash = { settings.lastCrash.collectAsState().value },
                                    onClickBack = { navHostController.navigateUp() },
                                    onClickShare = { crash ->
                                        val shareIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Version ${appMetadataProvider.versionCode}:\n$crash"
                                            )
                                            type = "text/plain"
                                        }.let {
                                            Intent.createChooser(it, shareTitle)
                                        }
                                        startActivity(shareIntent)
                                    }
                                )
                            }
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

            if (!featureOverrideProvider.isReleaseBuild
                && !settings.screenshotMode.collectAsState().value
            ) {
                Box(
                    modifier = Modifier
                        .background(colorResource(R.color.launcher_background))
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable(onClick = onVariantBannerClick)
                ) {
                    Text(
                        text = BuildConfig.BUILD_TYPE.toUpperCase(Locale.current),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        notificationsController.refresh()
    }
}
