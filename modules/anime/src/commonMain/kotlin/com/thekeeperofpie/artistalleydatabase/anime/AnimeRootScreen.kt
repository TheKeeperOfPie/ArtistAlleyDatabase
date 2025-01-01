package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_open_feature_tiers_button
import artistalleydatabase.modules.anime.generated.resources.anime_requires_unlock
import artistalleydatabase.modules.anime.generated.resources.anime_root_menu_history
import artistalleydatabase.modules.anime.generated.resources.anime_root_menu_ignored
import artistalleydatabase.modules.anime.generated.resources.last_crash_notification
import artistalleydatabase.modules.anime.generated.resources.last_crash_notification_button
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityEntry
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityList
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivitySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.characters.charactersSection
import com.thekeeperofpie.artistalleydatabase.anime.characters.horizontalCharactersRow
import com.thekeeperofpie.artistalleydatabase.anime.history.HistoryDestinations
import com.thekeeperofpie.artistalleydatabase.anime.home.AnimeHomeScreen
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreDestinations
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOptionRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.characterMediaItems
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.horizontalMediaCardRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.mediaHorizontalRow
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchEntry
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.search.SearchDestinations
import com.thekeeperofpie.artistalleydatabase.anime.search.SearchType
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDestinations
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffListRow
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudioDestinations
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudioListRow
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudioListRowFragmentEntry
import com.thekeeperofpie.artistalleydatabase.anime.studios.studiosSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.anime.users.UserDestinations
import com.thekeeperofpie.artistalleydatabase.anime.users.UserListRow
import com.thekeeperofpie.artistalleydatabase.anime.users.viewer.AniListViewerProfileScreen
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysNavigationBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalAppUpdateChecker
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigationBarEnterAlwaysScrollBehavior
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import kotlinx.coroutines.flow.StateFlow
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
        unlockedFlow: StateFlow<Boolean>,
        userRoute: UserRoute,
    ) {
        val scrollBehavior = navigationBarEnterAlwaysScrollBehavior()
        val bottomNavigationState = BottomNavigationState(scrollBehavior)
        val needsAuth = viewModel.authToken.collectAsStateWithLifecycle().value == null
        val persistedSelectedScreen by viewModel.persistedSelectedScreen.collectAsStateWithLifecycle()
        val unlocked by unlockedFlow.collectAsStateWithLifecycle()

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

                    AnimeRootNavDestination.entries
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
                                                        HistoryDestinations.MediaHistory(
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
                val component = LocalAnimeComponent.current
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
                            val animeSortFilterViewModel = viewModel {
                                component.animeSearchSortFilterViewModelFactory(
                                    createSavedStateHandle()
                                )
                                    .create(
                                        MediaSortFilterViewModel.InitialParams(
                                            sortClass = MediaSortOption::class,
                                            defaultSort = MediaSortOption.SEARCH_MATCH,
                                            mediaType = MediaType.ANIME,
                                        )
                                    )
                            }
                            val mangaSortFilterViewModel = viewModel {
                                component.mangaSearchSortFilterViewModelFactory(
                                    createSavedStateHandle()
                                )
                                    .create(
                                        MediaSortFilterViewModel.InitialParams(
                                            sortClass = MediaSortOption::class,
                                            defaultSort = MediaSortOption.SEARCH_MATCH,
                                            mediaType = MediaType.MANGA,
                                        )
                                    )
                            }
                            val characterSortFilterViewModel = viewModel {
                                component.characterSortFilterViewModel(
                                    createSavedStateHandle(),
                                    CharacterSortFilterViewModel.InitialParams()
                                )
                            }
                            val staffSortFilterViewModel = viewModel {
                                component.staffSortFilterViewModel(
                                    createSavedStateHandle(),
                                    StaffSortFilterViewModel.InitialParams()
                                )
                            }
                            val studiosSortFilterViewModel = viewModel {
                                component.studiosSortFilterViewModel(createSavedStateHandle())
                            }
                            val usersSortFilterViewModel = viewModel {
                                component.usersSortFilterViewModel(createSavedStateHandle())
                            }
                            val viewModel = viewModel {
                                component.animeSearchViewModelFactory(
                                    createSavedStateHandle(),
                                    unlockedFlow,
                                    animeSortFilterViewModel.state.filterParams,
                                    mangaSortFilterViewModel.state.filterParams,
                                    characterSortFilterViewModel.state.filterParams,
                                    staffSortFilterViewModel.state.filterParams,
                                    studiosSortFilterViewModel.state.filterParams,
                                    usersSortFilterViewModel.state.filterParams,
                                ).create(
                                    animeSortFilterViewModel::filterMedia,
                                    mangaSortFilterViewModel::filterMedia,
                                    MediaPreviewWithDescriptionEntry.Provider,
                                    MediaWithListStatusEntry.Provider,
                                    CharacterListRow.Entry.SearchProvider(),
                                    StaffListRow.Entry.SearchProvider(),
                                    StudioListRowFragmentEntry.Provider(),
                                    UserListRow.Entry.Provider(),
                                )
                            }
                            val state = AnimeSearchScreen.State(
                                unlocked = unlockedFlow,
                                selectedType = viewModel.selectedType,
                                query = viewModel.query,
                                mediaViewOption = viewModel.mediaViewOption,
                            )

                            val mediaViewOption by viewModel.mediaViewOption
                                .collectAsStateWithLifecycle()
                            val viewer by viewModel.viewer.collectAsStateWithLifecycle()
                            val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
                            AnimeSearchScreen(
                                mediaEditBottomSheetScaffold = MediaEditBottomSheetScaffold.fromComponent(component),
                                upIconOption = upIconOption,
                                onRefresh = viewModel::onRefresh,
                                content = viewModel.content.collectAsLazyPagingItems(),
                                animeSortFilterState = animeSortFilterViewModel.state,
                                mangaSortFilterState = mangaSortFilterViewModel.state,
                                characterSortFilterState = characterSortFilterViewModel.state,
                                staffSortFilterState = staffSortFilterViewModel.state,
                                studiosSortFilterState = studiosSortFilterViewModel.state,
                                usersSortFilterState = usersSortFilterViewModel.state,
                                state = state,
                                scrollStateSaver = ScrollStateSaver.fromMap(
                                    AnimeRootNavDestination.SEARCH.id,
                                    scrollPositions,
                                ),
                                bottomNavigationState = bottomNavigationState,
                                lockedFeatureTiers = { LockedFeatureTiers() },
                                item = { entry, onClickListEdit ->
                                    @Suppress("UNCHECKED_CAST")
                                    when (entry) {
                                        is AnimeSearchEntry.Media<*> -> MediaViewOptionRow(
                                            mediaViewOption = mediaViewOption,
                                            viewer = viewer,
                                            onClickListEdit = onClickListEdit,
                                            entry = entry.entry as? MediaPreviewWithDescriptionEntry,
                                        )
                                        is AnimeSearchEntry.Character<*> -> CharacterListRow(
                                            entry = entry.entry as? CharacterListRow.Entry<MediaWithListStatusEntry>,
                                            staffDetailsRoute =
                                                StaffDestinations.StaffDetails.route,
                                            mediaItems = {
                                                characterMediaItems(
                                                    characterId = entry.characterId,
                                                    media = it,
                                                    viewer = { viewer },
                                                    onClickListEdit = onClickListEdit,
                                                )
                                            },
                                        )
                                        is AnimeSearchEntry.Staff<*> ->
                                            SharedTransitionKeyScope("staff_list_row", entry.staffId) {
                                                StaffListRow(
                                                    entry = entry.entry as StaffListRow.Entry<MediaWithListStatusEntry>,
                                                    charactersSection = { horizontalCharactersRow(it) },
                                                    mediaSection = { media ->
                                                        horizontalMediaCardRow(
                                                            viewer = { viewer },
                                                            media = media,
                                                            onClickListEdit = onClickListEdit,
                                                        )
                                                    },
                                                )
                                            }
                                        is AnimeSearchEntry.Studio<*> ->
                                            SharedTransitionKeyScope("studio_list_row", entry.studioId) {
                                                StudioListRow(
                                                    entry = entry.entry as StudioListRowFragmentEntry<MediaWithListStatusEntry>,
                                                    mediaRow = { media ->
                                                        horizontalMediaCardRow(
                                                            viewer = { viewer },
                                                            media = media,
                                                            onClickListEdit = onClickListEdit,
                                                            mediaWidth = 120.dp,
                                                            mediaHeight = 180.dp,
                                                        )
                                                    },
                                                )
                                            }
                                        is AnimeSearchEntry.User<*> -> UserListRow(
                                            entry = entry.entry as UserListRow.Entry<MediaWithListStatusEntry>,
                                            mediaRow = { media ->
                                                horizontalMediaCardRow(
                                                    viewer = { viewer },
                                                    media = media,
                                                    onClickListEdit = onClickListEdit,
                                                    forceListEditIcon = true,
                                                )
                                            }
                                        )

                                        null -> when (selectedType) {
                                            SearchType.ANIME,
                                            SearchType.MANGA,
                                                -> MediaViewOptionRow(
                                                mediaViewOption = mediaViewOption,
                                                viewer = viewer,
                                                onClickListEdit = onClickListEdit,
                                                entry = null,
                                            )
                                            SearchType.CHARACTER ->
                                                CharacterListRow<Unit>(
                                                    entry = null,
                                                    staffDetailsRoute =
                                                        StaffDestinations.StaffDetails.route,
                                                    mediaItems = {},
                                                )
                                            SearchType.STAFF ->
                                                StaffListRow<Unit>(
                                                    entry = null,
                                                    charactersSection = {},
                                                    mediaSection = {},
                                                )
                                            SearchType.STUDIO ->
                                                StudioListRow<Unit>(
                                                    entry = null,
                                                    mediaRow = {},
                                                    mediaHeight = 180.dp,
                                                )
                                            SearchType.USER ->
                                                UserListRow(
                                                    entry = null,
                                                    mediaRow = { media ->
                                                        horizontalMediaCardRow(
                                                            viewer = { viewer },
                                                            media = media,
                                                            onClickListEdit = onClickListEdit,
                                                            forceListEditIcon = true,
                                                        )
                                                    }
                                                )
                                        }
                                    }
                                }
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
                                activitySortFilterViewModelProvider = {
                                    viewModel {
                                        component.activitySortFilterViewModel(
                                            createSavedStateHandle(),
                                            AnimeDestination.MediaDetails.route,
                                            ActivitySortFilterViewModel.InitialParams(
                                                // TODO: Re-evaluate this
                                                // Disable shared element otherwise the tab view will animate into the sort list
                                                mediaSharedElement = false,
                                                isMediaSpecific = false,
                                            ),
                                        )
                                    }
                                },
                                mediaWithListStatusEntryProvider = MediaWithListStatusEntry.Provider,
                                mediaCompactWithTagsEntryProvider = MediaCompactWithTagsEntry.Provider,
                                // TODO: Move this elsewhere to avoid remember
                                studioEntryProvider = remember { StudioListRowFragmentEntry.Provider() },
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
                                activitySection = { viewer, activities, sortFilterState, onActivityStatusUpdate, onClickListEdit, modifier ->
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
                                        sortFilterState = sortFilterState,
                                        userRoute = UserDestinations.User.route,
                                        mediaRow = { entry, modifier ->
                                            AnimeMediaCompactListRow(
                                                viewer = viewer,
                                                entry = entry,
                                                onClickListEdit = onClickListEdit,
                                                modifier = modifier,
                                            )
                                        },
                                        modifier = modifier,
                                    )
                                },
                                mediaDetailsRoute = AnimeDestination.MediaDetails.route,
                                searchMediaGenreRoute = SearchDestinations.SearchMedia.genreRoute,
                                searchMediaTagRoute = SearchDestinations.SearchMedia.tagRoute,
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

    @Composable
    private fun LockedFeatureTiers() {
        Box(
            contentAlignment = Alignment.Companion.Center,
            modifier = Modifier.Companion
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                Text(
                    text = stringResource(Res.string.anime_requires_unlock),
                    modifier = Modifier.Companion.padding(vertical = 10.dp)
                )
                val navigationController = LocalNavigationController.current
                Button(
                    onClick = {
                        navigationController.navigate(AnimeDestination.FeatureTiers)
                    }
                ) {
                    Text(
                        text = stringResource(
                            Res.string.anime_open_feature_tiers_button
                        )
                    )
                }
            }
        }
    }
}
