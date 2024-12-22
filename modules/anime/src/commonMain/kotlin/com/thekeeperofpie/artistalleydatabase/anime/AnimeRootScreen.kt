package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_root_menu_history
import artistalleydatabase.modules.anime.generated.resources.anime_root_menu_ignored
import artistalleydatabase.modules.anime.generated.resources.last_crash_notification
import artistalleydatabase.modules.anime.generated.resources.last_crash_notification_button
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityEntry
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityList
import com.thekeeperofpie.artistalleydatabase.anime.characters.charactersSection
import com.thekeeperofpie.artistalleydatabase.anime.home.AnimeHomeScreen
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreDestinations
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.horizontalMediaCardRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.mediaHorizontalRow
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDestinations
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudioDestinations
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudioListRowFragmentEntry
import com.thekeeperofpie.artistalleydatabase.anime.studios.studiosSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.anime.users.UserDestinations
import com.thekeeperofpie.artistalleydatabase.anime.users.viewer.AniListViewerProfileScreen
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysNavigationBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalAppUpdateChecker
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigationBarEnterAlwaysScrollBehavior
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import org.jetbrains.compose.resources.stringResource

object AnimeRootScreen {

    @Composable
    operator fun invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        upIconOption: UpIconOption?,
        viewModel: AnimeRootViewModel,
        onClickAuth: () -> Unit,
        onSubmitAuthToken: (String) -> Unit,
        onClickSettings: () -> Unit,
        onClickShowLastCrash: () -> Unit,
        userRoute: UserRoute,
    ) {
        val scrollBehavior = navigationBarEnterAlwaysScrollBehavior()
        val bottomNavigationState = BottomNavigationState(scrollBehavior)
        val needsAuth = viewModel.authToken.collectAsStateWithLifecycle().value == null
        val persistedSelectedScreen by viewModel.persistedSelectedScreen.collectAsStateWithLifecycle()
        val unlocked by viewModel.unlocked.collectAsStateWithLifecycle()

        var selectedScreen by rememberSaveable(stateSaver = AnimeRootNavDestination.StateSaver) {
            mutableStateOf(
                persistedSelectedScreen
                    .takeIf { !it.requiresAuth || !needsAuth }
                    ?.takeIf { !it.requiresUnlock || unlocked }
                    ?: AnimeRootNavDestination.HOME
            )
        }

        @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold(
            snackbarHost = {
                val appUpdateChecker = LocalAppUpdateChecker.current
                val snackbarHostState = remember { SnackbarHostState() }
                val lastCrashShown by viewModel.lastCrashShown.collectAsStateWithLifecycle()
                val lastCrashText by viewModel.lastCrash.collectAsStateWithLifecycle()
                val lastCrashMessage = stringResource(Res.string.last_crash_notification)
                val lastCrashButton = stringResource(Res.string.last_crash_notification_button)
                LaunchedEffect(lastCrashText, lastCrashShown) {
                    if (lastCrashText.isNotBlank() && !lastCrashShown) {
                        val result = snackbarHostState.showSnackbar(
                            message = lastCrashMessage,
                            actionLabel = lastCrashButton,
                            withDismissAction = true,
                            duration = SnackbarDuration.Long,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            onClickShowLastCrash()
                        } else {
                            viewModel.lastCrashShown.value = true
                        }
                    }
                }

                if (lastCrashText.isBlank() || lastCrashShown) {
                    appUpdateChecker?.applySnackbarState(snackbarHostState)
                }
                SnackbarHost(hostState = snackbarHostState)
            },
            bottomBar = {
                EnterAlwaysNavigationBar(
                    scrollBehavior = scrollBehavior,
                    modifier = Modifier.height(56.dp)
                ) {
                    val navigationController = LocalNavigationController.current

                    var showAnimeMenu by remember { mutableStateOf(false) }
                    var showMangaMenu by remember { mutableStateOf(false) }

                    fun dismissMenu(destination: AnimeRootNavDestination) {
                        if (destination == AnimeRootNavDestination.ANIME) {
                            showAnimeMenu = false
                        } else {
                            showMangaMenu = false
                        }
                    }

                    AnimeRootNavDestination.values()
                        .filter { !it.requiresAuth || !needsAuth }
                        .filter { !it.requiresUnlock || unlocked }
                        .filter { it != AnimeRootNavDestination.UNLOCK || !unlocked }
                        .forEach { destination ->
                            NavigationBarItem(
                                icon = {
                                    Icon(destination.icon, contentDescription = null)
                                    if (destination == AnimeRootNavDestination.ANIME
                                        || destination == AnimeRootNavDestination.MANGA
                                    ) {
                                        DropdownMenu(
                                            expanded = if (destination == AnimeRootNavDestination.ANIME) {
                                                showAnimeMenu
                                            } else {
                                                showMangaMenu
                                            },
                                            onDismissRequest = { dismissMenu(destination) },
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(Res.string.anime_root_menu_ignored)) },
                                                onClick = {
                                                    dismissMenu(destination)
                                                    navigationController.navigate(
                                                        IgnoreDestinations.Ignored(
                                                            mediaType = if (destination == AnimeRootNavDestination.ANIME) {
                                                                MediaType.ANIME
                                                            } else {
                                                                MediaType.MANGA
                                                            }
                                                        )
                                                    )
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = { Text(stringResource(Res.string.anime_root_menu_history)) },
                                                onClick = {
                                                    dismissMenu(destination)
                                                    navigationController.navigate(
                                                        AnimeDestination.MediaHistory(
                                                            mediaType = if (destination == AnimeRootNavDestination.ANIME) {
                                                                MediaType.ANIME
                                                            } else {
                                                                MediaType.MANGA
                                                            },
                                                        )
                                                    )
                                                },
                                            )
                                        }
                                    }
                                },
                                selected = selectedScreen == destination,
                                onClick = {
                                    if (selectedScreen == destination) {
                                        when (destination) {
                                            AnimeRootNavDestination.ANIME -> showAnimeMenu = true
                                            AnimeRootNavDestination.MANGA -> showMangaMenu = true
                                            AnimeRootNavDestination.HOME,
                                            AnimeRootNavDestination.SEARCH,
                                            AnimeRootNavDestination.PROFILE,
                                            AnimeRootNavDestination.UNLOCK,
                                                -> Unit
                                        }
                                    } else {
                                        selectedScreen = destination
                                    }
                                },
                            )
                        }
                }
            },
        ) {
            val scrollPositions = ScrollStateSaver.scrollPositions()
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = selectedScreen,
                    transitionSpec = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                            .togetherWith(
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Down
                                )
                            )
                    },
                    label = "Anime home destination transition",
                ) {
                    when (it) {
                        AnimeRootNavDestination.HOME -> AnimeHomeScreen(
                            upIconOption = upIconOption,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                AnimeRootNavDestination.HOME.id,
                                scrollPositions
                            ),
                            bottomNavigationState = bottomNavigationState,
                            userRoute = userRoute,
                        )
                        AnimeRootNavDestination.ANIME -> AnimeNavigator.UserMediaListScreen(
                            userId = null,
                            userName = null,
                            mediaType = MediaType.ANIME,
                            upIconOption = upIconOption,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                AnimeRootNavDestination.ANIME.id,
                                scrollPositions
                            ),
                            bottomNavigationState = bottomNavigationState,
                        )
                        AnimeRootNavDestination.MANGA -> AnimeNavigator.UserMediaListScreen(
                            userId = null,
                            userName = null,
                            mediaType = MediaType.MANGA,
                            upIconOption = upIconOption,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                AnimeRootNavDestination.MANGA.id,
                                scrollPositions
                            ),
                            bottomNavigationState = bottomNavigationState,
                        )
                        AnimeRootNavDestination.SEARCH -> {
                            AnimeSearchScreen(
                                upIconOption = upIconOption,
                                scrollStateSaver = ScrollStateSaver.fromMap(
                                    AnimeRootNavDestination.SEARCH.id,
                                    scrollPositions,
                                ),
                                bottomNavigationState = bottomNavigationState,
                            )
                        }
                        AnimeRootNavDestination.PROFILE -> {
                            val navigationController = LocalNavigationController.current
                            val activityEntryProvider = remember {
                                ActivityEntry.provider(MediaCompactWithTagsEntry.Provider)
                            }
                            AniListViewerProfileScreen(
                                component = LocalAnimeComponent.current,
                                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                                upIconOption = upIconOption,
                                needsAuth = { needsAuth },
                                onClickAuth = onClickAuth,
                                onSubmitAuthToken = onSubmitAuthToken,
                                onClickSettings = onClickSettings,
                                bottomNavigationState = bottomNavigationState,
                                // TODO: Move this elsewhere to avoid remember
                                activityEntryProvider = activityEntryProvider,
                                mediaWithListStatusEntryProvider = MediaWithListStatusEntry.Provider,
                                mediaCompactWithTagsEntryProvider = MediaCompactWithTagsEntry.Provider,
                                // TODO: Move this elsewhere to avoid remember
                                studioEntryProvider = remember { StudioListRowFragmentEntry.provider() },
                                mediaHorizontalRow = { viewer, titleRes, entries, viewAllRoute, viewAllContentDescriptionTextRes, onClickListEdit ->
                                    // TODO: mediaListEntry doesn't load properly for these, figure out a way to show status
                                    mediaHorizontalRow(
                                        viewer = viewer,
                                        titleRes = titleRes,
                                        entries = entries,
                                        forceListEditIcon = true,
                                        onClickListEdit = onClickListEdit,
                                        onClickViewAll = {
                                            navigationController.navigate(viewAllRoute)
                                        },
                                        viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
                                    )
                                },
                                charactersSection = { titleRes, characters, viewAllRoute, viewAllContentDescriptionTextRes ->
                                    charactersSection(
                                        titleRes = titleRes,
                                        characters = characters,
                                        viewAllRoute = viewAllRoute,
                                        viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
                                        staffDetailsRoute = StaffDestinations.StaffDetails.route,
                                    )
                                },
                                staffSection = { titleRes, staff, viewAllRoute, viewAllContentDescriptionTextRes ->
                                    staffSection(
                                        titleRes = titleRes,
                                        staffList = staff,
                                        viewAllRoute = viewAllRoute,
                                        viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
                                    )
                                },
                                studiosSection = { viewer, studios, hasMore, onClickListEdit ->
                                    studiosSection(
                                        studios = studios,
                                        hasMore = hasMore,
                                        mediaRow = { media ->
                                            horizontalMediaCardRow(
                                                viewer = { viewer },
                                                media = media,
                                                onClickListEdit = onClickListEdit,
                                                mediaWidth = 64.dp,
                                                mediaHeight = 96.dp,
                                            )
                                        },
                                    )
                                },
                                activitySection = { viewer, activities, sortFilterState, onActivityStatusUpdate, onClickListEdit ->
                                    ActivityList(
                                        viewer = viewer,
                                        activities = activities,
                                        entryToActivity = activityEntryProvider::activity,
                                        activityId = activityEntryProvider::id,
                                        activityContentType = activityEntryProvider::contentType,
                                        activityToMediaEntry = activityEntryProvider::media,
                                        activityStatusAware = activityEntryProvider::activityStatusAware,
                                        onActivityStatusUpdate = onActivityStatusUpdate,
                                        showMedia = true,
                                        allowUserClick = false,
                                        sortFilterState = { sortFilterState },
                                        userRoute = UserDestinations.User.route,
                                        mediaRow = { entry, modifier ->
                                            AnimeMediaCompactListRow(
                                                viewer = viewer,
                                                entry = entry,
                                                onClickListEdit = onClickListEdit,
                                                modifier = modifier,
                                            )
                                        },
                                    )
                                },
                                mediaDetailsRoute = AnimeDestination.MediaDetails.route,
                                searchMediaGenreRoute = AnimeDestination.SearchMedia.genreRoute,
                                searchMediaTagRoute = AnimeDestination.SearchMedia.tagRoute,
                                staffDetailsRoute = StaffDestinations.StaffDetails.route,
                                studioMediasRoute = StudioDestinations.StudioMedias.route,
                            )
                        }
                        AnimeRootNavDestination.UNLOCK -> {
                            val animeComponent = LocalAnimeComponent.current
                            UnlockScreen(
                                upIconOption = upIconOption,
                                viewModel = viewModel { animeComponent.unlockScreenViewModel() },
                                bottomNavigationState = bottomNavigationState,
                                onClickSettings = onClickSettings,
                            )
                        }
                    }
                }
            }
        }
    }
}
