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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.anilist.type.MediaType
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.thekeeperofpie.anichive.BuildConfig
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.android_utils.AppMetadataProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionCharacters
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionStaff
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionVoiceActor
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComposeSettings
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.markdown.LocalMarkwon
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGenrePreview
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaTagPreview
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsController
import com.thekeeperofpie.artistalleydatabase.anime.utils.FullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.anime2anime.Anime2AnimeScreen
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseViewModel
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryNavigator
import com.thekeeperofpie.artistalleydatabase.compose.DoubleDrawerValue
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.image.LocalImageColorsState
import com.thekeeperofpie.artistalleydatabase.compose.image.rememberImageColorsState
import com.thekeeperofpie.artistalleydatabase.compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.compose.rememberDrawerState
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.compose.update.AppUpdateChecker
import com.thekeeperofpie.artistalleydatabase.compose.update.LocalAppUpdateChecker
import com.thekeeperofpie.artistalleydatabase.export.ExportScreen
import com.thekeeperofpie.artistalleydatabase.importing.ImportScreen
import com.thekeeperofpie.artistalleydatabase.monetization.LocalMonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.LocalSubscriptionProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.settings.SettingsScreen
import com.thekeeperofpie.artistalleydatabase.settings.SettingsViewModel
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseSyncWorker
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch
import java.util.Optional
import javax.inject.Inject
import kotlin.enums.EnumEntries
import kotlin.jvm.optionals.getOrNull

@OptIn(ExperimentalSharedTransitionApi::class)
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

    @Inject
    lateinit var monetizationController: MonetizationController

    @Inject
    lateinit var monetizationProviderOptional: Optional<MonetizationProvider>

    @Inject
    lateinit var subscriptionProviderOptional: Optional<SubscriptionProvider>

    @Inject
    lateinit var appUpdateCheckerOptional: Optional<AppUpdateChecker>

    @Inject
    lateinit var featureOverrideProvider: FeatureOverrideProvider

    @Inject
    lateinit var mediaTagDialogController: MediaTagDialogController

    @Inject
    lateinit var mediaGenreDialogController: MediaGenreDialogController

    @Inject
    lateinit var appMetadataProvider: AppMetadataProvider

    @Inject
    lateinit var markwon: Markwon

    @Inject
    lateinit var navigationTypeMap: NavigationTypeMap

    @Inject
    lateinit var notificationsController: NotificationsController

    @Inject
    lateinit var ignoreController: IgnoreController

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
        val monetizationProvider = monetizationProviderOptional.getOrNull()
        monetizationProvider?.initialize(this)
        val subscriptionProvider = subscriptionProviderOptional.getOrNull()
        subscriptionProvider?.initialize(this)
        val appUpdateChecker = appUpdateCheckerOptional.getOrNull()
        appUpdateChecker?.initialize(this)

        setContent {
            val navHostController = rememberNavController()
            val languageOptionMedia by settings.languageOptionMedia.collectAsState()
            val languageOptionCharacters by settings.languageOptionCharacters.collectAsState()
            val languageOptionStaff by settings.languageOptionStaff.collectAsState()
            val languageOptionVoiceActor by settings.languageOptionVoiceActor.collectAsState()
            val showFallbackVoiceActor by settings.showFallbackVoiceActor.collectAsState()

            val navigationCallback =
                remember(languageOptionMedia, languageOptionCharacters, languageOptionStaff) {
                    AnimeNavigator.NavigationCallback(
                        navHostController = navHostController,
                        cdEntryNavigator = cdEntryNavigator,
                    )
                }

            ArtistAlleyDatabaseTheme(settings = settings, navHostController = navHostController) {
                val imageColorsState = rememberImageColorsState()
                CompositionLocalProvider(
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
                    LocalNavigationCallback provides navigationCallback,
                    LocalFullscreenImageHandler provides fullScreenImageHandler,
                    LocalMarkwon provides markwon,
                    LocalAnimeComposeSettings provides settings.composeSettingsData(),
                    LocalImageColorsState provides imageColorsState,
                    LocalIgnoreController provides ignoreController,
                ) {
                    // TODO: Draw inside insets for applicable screens
                    Surface(modifier = Modifier.safeDrawingPadding()) {
                        val drawerState = rememberDrawerState(DoubleDrawerValue.Closed)
                        val scope = rememberCoroutineScope()
                        val navDrawerItems = NavDrawerItems.entries

                        fun onClickNav() = scope.launch { drawerState.openStart() }
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
                                drawerState = drawerState,
                                gesturesEnabled = gesturesEnabled,
                                drawerContent = {
                                    // TODO: This is not a good way to to infer the selected root
                                    var selectedRouteIndex by rememberSaveable {
                                        mutableIntStateOf(
                                            NavDrawerItems.entries
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
                                        onCloseDrawer = { scope.launch { drawerState.close() } },
                                    )
                                }
                            ) {
                                Content(
                                    navHostController = navHostController,
                                    unlockDatabaseFeatures = true,
                                    onClickNav = ::onClickNav,
                                    startDestination = startDestination,
                                    onVariantBannerClick = {
                                        scope.launch { drawerState.openEnd() }
                                    },
                                )
                            }
                        } else {
                            Content(
                                navHostController = navHostController,
                                unlockDatabaseFeatures = false,
                                onClickNav = ::onClickNav,
                                startDestination = startDestination,
                                onVariantBannerClick = { scope.launch { drawerState.openEnd() } },
                            )
                        }
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

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hideStatusBar) {
            window.insetsController?.run {
                hide(WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    @Composable
    private fun StartDrawer(
        navDrawerItems: () -> EnumEntries<NavDrawerItems>,
        selectedIndex: () -> Int,
        onSelectIndex: (Int) -> Unit,
        onCloseDrawer: () -> Unit,
    ) {
        ModalDrawerSheet {
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
                            Text(stringResource(R.string.nav_drawer_manga))
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
                            AnimeNavigator.initialize(
                                navHostController = navHostController,
                                navGraphBuilder = this,
                                upIconOption = navDrawerUpIconOption,
                                navigationTypeMap = navigationTypeMap,
                                onClickAuth = {
                                    aniListOAuthStore.launchAuthRequest(
                                        this@MainActivity
                                    )
                                },
                                onClickSettings = {
                                    navHostController.navigate(AppNavDestinations.SETTINGS.id)
                                },
                                onClickShowLastCrash = {
                                    navHostController.navigate(AppNavDestinations.CRASH.id)
                                }
                            )

                            artEntryNavigator.initialize(
                                onClickNav = onClickNav,
                                navHostController = navHostController,
                                navGraphBuilder = this
                            )
                            cdEntryNavigator.initialize(
                                onClickNav = onClickNav,
                                navHostController = navHostController,
                                navGraphBuilder = this
                            )

                            sharedElementComposable(AppNavDestinations.BROWSE.id) {
                                val viewModel = hiltViewModel<BrowseViewModel>()
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
                                ImportScreen(upIconOption = navDrawerUpIconOption)
                            }

                            sharedElementComposable(AppNavDestinations.EXPORT.id) {
                                ExportScreen(upIconOption = navDrawerUpIconOption)
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
                                    hiltViewModel<SettingsViewModel>().apply {
                                        initialize(
                                            onClickDatabaseFetch = {
                                                val request =
                                                    OneTimeWorkRequestBuilder<DatabaseSyncWorker>()
                                                        .setExpedited(
                                                            OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
                                                        )
                                                        .build()

                                                it.enqueueUniqueWork(
                                                    DatabaseSyncWorker.UNIQUE_WORK_NAME,
                                                    ExistingWorkPolicy.REPLACE,
                                                    request
                                                )
                                            }
                                        )
                                    }
                                val root = it.arguments?.getString("root")
                                    ?.toBooleanStrictOrNull() == true
                                val navigationCallback = LocalNavigationCallback.current
                                SettingsScreen(
                                    viewModel = viewModel,
                                    appMetadataProvider = appMetadataProvider,
                                    upIconOption = navDrawerUpIconOption.takeIf { root }
                                        ?: UpIconOption.Back(navHostController),
                                    onClickShowLastCrash = {
                                        navHostController.navigate(AppNavDestinations.CRASH.id)
                                    },
                                    onClickShowLicenses = {
                                        // TODO: Better UI for licenses
                                        startActivity(
                                            Intent(
                                                this@MainActivity,
                                                OssLicensesMenuActivity::class.java,
                                            ).setClassName(
                                                this@MainActivity,
                                                OssLicensesMenuActivity::class.java.canonicalName!!,
                                            )
                                        )
                                    },
                                    onClickFeatureTiers = {
                                        navigationCallback.navigate(AnimeDestination.FeatureTiers)
                                    },
                                    onClickViewMediaHistory = {
                                        navigationCallback.navigate(
                                            AnimeDestination.MediaHistory(mediaType = null)
                                        )
                                    },
                                    onClickViewMediaIgnore = {
                                        navigationCallback.navigate(
                                            AnimeDestination.Ignored(mediaType = null)
                                        )
                                    },
                                )
                            }

                            sharedElementComposable(AppNavDestinations.ANIME_2_ANIME.id) {
                                Anime2AnimeScreen(upIconOption = navDrawerUpIconOption)
                            }

                            sharedElementComposable(
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
                                    appMetadataProvider = appMetadataProvider,
                                    onClickBack = { navHostController.navigateUp() },
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
