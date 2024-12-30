package com.thekeeperofpie.artistalleydatabase.anime.users.viewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.users.generated.resources.Res
import artistalleydatabase.modules.anime.users.generated.resources.anime_auth_button_log_in
import artistalleydatabase.modules.anime.users.generated.resources.anime_auth_prompt_label
import artistalleydatabase.modules.anime.users.generated.resources.anime_auth_prompt_paste
import artistalleydatabase.modules.anime.users.generated.resources.anime_auth_prompt_text
import artistalleydatabase.modules.anime.users.generated.resources.anime_settings_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.confirm
import com.anilist.data.fragment.MediaCompactWithTags
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaWithListStatus
import com.anilist.data.fragment.StudioListRowFragment
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivitySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityToggleUpdate
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterDetails
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffDetails
import com.thekeeperofpie.artistalleydatabase.anime.studios.data.StudioEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.ui.SearchMediaGenreRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.SearchMediaTagRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.StudioMediasRoute
import com.thekeeperofpie.artistalleydatabase.anime.users.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.users.AniListUserViewModel
import com.thekeeperofpie.artistalleydatabase.anime.users.UserHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.users.UsersComponent
import com.thekeeperofpie.artistalleydatabase.anime.users.social.UserSocialViewModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object AniListViewerProfileScreen {

    @Composable
    operator fun <ActivityEntry : Any, MediaWithListStatusEntry : Any, MediaCompactWithTagsEntry : Any, StudioEntry : Any> invoke(
        component: UsersComponent,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        upIconOption: UpIconOption?,
        needsAuth: @Composable () -> Boolean,
        onClickAuth: () -> Unit,
        onSubmitAuthToken: (String) -> Unit,
        onClickSettings: () -> Unit,
        activityEntryProvider: ActivityEntryProvider<ActivityEntry, MediaCompactWithTagsEntry>,
        activitySortFilterViewModelProvider: @Composable () -> ActivitySortFilterViewModel,
        mediaWithListStatusEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaWithListStatusEntry>,
        mediaCompactWithTagsEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaCompactWithTagsEntry>,
        studioEntryProvider: StudioEntryProvider<StudioListRowFragment, StudioEntry, MediaWithListStatusEntry>,
        mediaHorizontalRow: LazyGridScope.(
            AniListViewer?,
            titleRes: StringResource,
            LazyPagingItems<MediaWithListStatusEntry>,
            viewAllRoute: NavDestination,
            viewAllContentDescriptionTextRes: StringResource,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        charactersSection: LazyGridScope.(
            titleRes: StringResource,
            characters: LazyPagingItems<CharacterDetails>,
            viewAllRoute: (() -> NavDestination)?,
            viewAllContentDescriptionTextRes: StringResource,
        ) -> Unit,
        staffSection: LazyGridScope.(
            titleRes: StringResource?,
            staff: LazyPagingItems<StaffDetails>,
            viewAllRoute: NavDestination,
            viewAllContentDescriptionTextRes: StringResource,
        ) -> Unit,
        studiosSection: LazyGridScope.(
            AniListViewer?,
            List<StudioEntry>,
            hasMore: Boolean,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        activitySection: @Composable (
            AniListViewer?,
            activities: LazyPagingItems<ActivityEntry>,
            sortFilterState: SortFilterState<*>,
            onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
        mediaDetailsRoute: MediaDetailsRoute,
        searchMediaGenreRoute: SearchMediaGenreRoute,
        searchMediaTagRoute: SearchMediaTagRoute,
        staffDetailsRoute: StaffDetailsRoute,
        studioMediasRoute: StudioMediasRoute,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        if (needsAuth()) {
            AuthPrompt(
                onClickAuth = onClickAuth,
                onSubmitAuthToken = onSubmitAuthToken,
                onClickSettings = onClickSettings,
                bottomNavigationState = bottomNavigationState,
            )
        } else {
            val activitySortFilterViewModel = activitySortFilterViewModelProvider()
            val viewModel = viewModel {
                component.aniListUserViewModelFactory(createSavedStateHandle())
                    .create(
                        activityEntryProvider,
                        activitySortFilterViewModel,
                        mediaWithListStatusEntryProvider,
                        mediaCompactWithTagsEntryProvider,
                        studioEntryProvider,
                    )
            }
            // TODO: Remove ViewModels from this file
            val entry by viewModel.entry.collectAsState()
            val headerValues = UserHeaderValues(null) { entry.result?.user }
            val followingViewModel =
                viewModel { component.userSocialViewModelFollowing(viewModel.userId) }
            val followersViewModel =
                viewModel { component.userSocialViewModelFollowers(viewModel.userId) }
            val viewer by viewModel.viewer.collectAsState()
            val activities = viewModel.activities.collectAsLazyPagingItems()
            UserScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                viewModel = viewModel,
                activitySortFilterViewModel = activitySortFilterViewModel,
                followingViewModel = followingViewModel,
                followersViewModel = followersViewModel,
                upIconOption = upIconOption,
                headerValues = headerValues,
                showLogOut = true,
                bottomNavigationState = bottomNavigationState,
                onClickSettings = onClickSettings,
                mediaHorizontalRow = { titleRes, entries, viewAllRoute, viewAllContentDescriptionTextRes, onClickListEdit ->
                    mediaHorizontalRow(
                        viewer,
                        titleRes,
                        entries,
                        viewAllRoute,
                        viewAllContentDescriptionTextRes,
                        onClickListEdit,
                    )
                },
                charactersSection = charactersSection,
                staffSection = staffSection,
                studiosSection = { studios, hasMore, onClickListEdit ->
                    studiosSection(viewer, studios, hasMore, onClickListEdit)
                },
                activitySection = { onClickListEdit, modifier ->
                    activitySection(
                        viewer,
                        activities,
                        activitySortFilterViewModel.state,
                        viewModel.activityToggleHelper::toggle,
                        onClickListEdit,
                        modifier,
                    )
                },
                mediaDetailsRoute = mediaDetailsRoute,
                searchMediaGenreRoute = searchMediaGenreRoute,
                searchMediaTagRoute = searchMediaTagRoute,
                staffDetailsRoute = staffDetailsRoute,
                studioMediasRoute = studioMediasRoute,
            )
        }
    }

    // TODO: Move auth to separate module and slot API
    @Composable
    private fun AuthPrompt(
        onClickAuth: () -> Unit,
        onSubmitAuthToken: (String) -> Unit,
        onClickSettings: (() -> Unit)?,
        bottomNavigationState: BottomNavigationState?,
    ) {
        Scaffold(
            modifier = Modifier.conditionally(bottomNavigationState != null) {
                nestedScroll(bottomNavigationState!!.nestedScrollConnection)
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .widthIn(min = 300.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        stringResource(Res.string.anime_auth_prompt_label),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 32.dp)
                    )

                    Text(
                        stringResource(Res.string.anime_auth_prompt_text),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .widthIn(min = 300.dp)
                            .width(IntrinsicSize.Min)
                    )

                    FilledTonalButton(onClick = onClickAuth) {
                        Text(stringResource(Res.string.anime_auth_button_log_in))
                    }

                    Text(
                        stringResource(Res.string.anime_auth_prompt_paste),
                        modifier = Modifier.padding(top = 20.dp)
                    )

                    var value by remember { mutableStateOf("") }
                    TextField(
                        value = value,
                        onValueChange = { value = it },
                        modifier = Modifier
                            .size(width = 200.dp, height = 200.dp)
                            .padding(16.dp),
                    )

                    FilledTonalButton(onClick = {
                        val token = value
                        value = ""
                        onSubmitAuthToken(token)
                    }) {
                        Text(stringResource(UtilsStrings.confirm))
                    }

                    Spacer(Modifier.height(88.dp))
                }

                if (onClickSettings != null) {
                    IconButton(
                        onClick = onClickSettings,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(
                                Res.string.anime_settings_content_description
                            )
                        )
                    }
                }
            }
        }
    }

    // TODO: Share this elsewhere
    @Composable
    fun <ActivityEntry : Any, MediaWithListStatusEntry : Any, MediaCompactWithTagsEntry, StudioEntry : Any> UserScreen(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        viewModel: AniListUserViewModel<ActivityEntry, MediaWithListStatusEntry, MediaCompactWithTagsEntry, StudioEntry>,
        activitySortFilterViewModel: ActivitySortFilterViewModel,
        followingViewModel: UserSocialViewModel.Following,
        followersViewModel: UserSocialViewModel.Followers,
        upIconOption: UpIconOption?,
        headerValues: UserHeaderValues,
        mediaHorizontalRow: LazyGridScope.(
            titleRes: StringResource,
            LazyPagingItems<MediaWithListStatusEntry>,
            viewAllRoute: NavDestination,
            viewAllContentDescriptionTextRes: StringResource,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        charactersSection: LazyGridScope.(
            titleRes: StringResource,
            characters: LazyPagingItems<CharacterDetails>,
            viewAllRoute: (() -> NavDestination)?,
            viewAllContentDescriptionTextRes: StringResource,
        ) -> Unit,
        staffSection: LazyGridScope.(
            titleRes: StringResource?,
            staff: LazyPagingItems<StaffDetails>,
            viewAllRoute: NavDestination,
            viewAllContentDescriptionTextRes: StringResource,
        ) -> Unit,
        studiosSection: LazyGridScope.(
            List<StudioEntry>,
            hasMore: Boolean,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        activitySection: @Composable (
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
        mediaDetailsRoute: MediaDetailsRoute,
        searchMediaGenreRoute: SearchMediaGenreRoute,
        searchMediaTagRoute: SearchMediaTagRoute,
        staffDetailsRoute: StaffDetailsRoute,
        studioMediasRoute: StudioMediasRoute,
        showLogOut: Boolean = false,
        bottomNavigationState: BottomNavigationState? = null,
        onClickSettings: (() -> Unit)? = null,
    ) {
        val entry by viewModel.entry.collectAsState()
        val viewer by viewModel.viewer.collectAsState()
        val anime = viewModel.anime.collectAsLazyPagingItems()
        val manga = viewModel.manga.collectAsLazyPagingItems()
        val characters = viewModel.characters.collectAsLazyPagingItems()
        val staff = viewModel.staff.collectAsLazyPagingItems()
        val studios by viewModel.studios.collectAsState()
        AniListUserScreen(
            mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
            viewer = { viewer },
            userId = viewModel.userId,
            isFollowing = { viewModel.isFollowing() },
            onFollowingToggle = viewModel::toggleFollow,
            onRefresh = viewModel::refresh,
            entry = { entry },
            animeGenresState = { viewModel.animeStats.genresState },
            animeStaffState = { viewModel.animeStats.staffState },
            animeTagsState = { viewModel.animeStats.tagsState },
            animeVoiceActorsState = { viewModel.animeStats.voiceActorsState },
            animeStudiosState = { viewModel.animeStats.studiosState },
            mangaGenresState = { viewModel.mangaStats.genresState },
            mangaStaffState = { viewModel.mangaStats.staffState },
            mangaTagsState = { viewModel.mangaStats.tagsState },
            upIconOption = upIconOption,
            headerValues = headerValues,
            anime = anime,
            manga = manga,
            // TODO: mediaListEntry doesn't load properly for these, figure out a way to show status
            mediaHorizontalRow = mediaHorizontalRow,
            characters = characters,
            charactersSection = charactersSection,
            staff = staff,
            staffSection = staffSection,
            studios = { studios },
            studiosSection = studiosSection,
            activitySortFilterState = activitySortFilterViewModel.state,
            activitySection = activitySection,
            socialFollowing = followingViewModel.data().collectAsLazyPagingItems(),
            socialFollowers = followersViewModel.data().collectAsLazyPagingItems(),
            showLogOut = showLogOut,
            onLogOutClick = viewModel::logOut,
            bottomNavigationState = bottomNavigationState,
            onClickSettings = onClickSettings,
            mediaDetailsRoute = mediaDetailsRoute,
            searchMediaGenreRoute = searchMediaGenreRoute,
            searchMediaTagRoute = searchMediaTagRoute,
            staffDetailsRoute = staffDetailsRoute,
            studioMediasRoute = studioMediasRoute,
        )
    }
}
