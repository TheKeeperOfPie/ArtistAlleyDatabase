package com.thekeeperofpie.artistalleydatabase.anime.users

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import artistalleydatabase.modules.anime.users.generated.resources.Res
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_anime_user
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_anime_you
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_characters_user
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_characters_you
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_manga_user
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_manga_you
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_staff_user
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_staff_you
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_studios_user
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_studios_you
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_followers_user
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_followers_you
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_following_user
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_following_you
import com.anilist.data.UserFavoritesCharactersQuery
import com.anilist.data.UserFavoritesStaffQuery
import com.anilist.data.fragment.MediaCompactWithTags
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaPreviewWithDescription
import com.anilist.data.fragment.MediaWithListStatus
import com.anilist.data.fragment.StudioListRowFragment
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivitySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityToggleUpdate
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterDetails
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffDetails
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.studios.data.StudioEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.ui.SearchMediaGenreRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.SearchMediaTagRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.StudioMediasRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.anime.users.favorite.UserFavoriteCharactersScreen
import com.thekeeperofpie.artistalleydatabase.anime.users.favorite.UserFavoriteMediaScreen
import com.thekeeperofpie.artistalleydatabase.anime.users.favorite.UserFavoriteStaffScreen
import com.thekeeperofpie.artistalleydatabase.anime.users.favorite.UserFavoriteStudiosScreen
import com.thekeeperofpie.artistalleydatabase.anime.users.follow.UserListScreen
import com.thekeeperofpie.artistalleydatabase.anime.users.viewer.AniListViewerProfileScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object UserDestinations {

    @Serializable
    data class User(
        val userId: String? = null,
        val sharedTransitionKey: SharedTransitionKey? = null,
        val headerParams: UserHeaderParams? = null,
    ) : NavDestination {
        companion object {
            val route: UserRoute = { id, userSharedTransitionKey, name, imageState ->
                User(
                    userId = id,
                    sharedTransitionKey = userSharedTransitionKey,
                    headerParams = UserHeaderParams(
                        name = name,
                        bannerImage = null,
                        coverImage = imageState,
                    )
                )
            }
        }
    }

    @Serializable
    data class UserFavoriteCharacters(
        val userId: String?,
        val userName: String? = null,
    ) : NavDestination

    @Serializable
    data class UserFavoriteMedia(
        val userId: String?,
        val userName: String? = null,
        val mediaType: MediaType,
    ) : NavDestination

    @Serializable
    data class UserFavoriteStaff(
        val userId: String?,
        val userName: String? = null,
    ) : NavDestination

    @Serializable
    data class UserFavoriteStudios(
        val userId: String?,
        val userName: String? = null,
    ) : NavDestination

    @Serializable
    data class UserFollowers(
        val userId: String?,
        val userName: String? = null,
    ) : NavDestination

    @Serializable
    data class UserFollowing(
        val userId: String?,
        val userName: String? = null,
    ) : NavDestination

    @Serializable
    data class UserList(
        val userId: String?,
        val userName: String?,
        val mediaType: MediaType,
        val mediaListStatus: MediaListStatus? = null,
    ) : NavDestination

    fun <ActivityEntry : Any, CharacterEntry : Any, MediaWithListStatusEntry : Any, MediaCompactWithTagsEntry : Any, MediaPreviewWithDescriptionEntry : Any, StaffEntry : Any, StudioEntry : Any> addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: UsersComponent,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        mediaDetailsRoute: MediaDetailsRoute,
        searchMediaGenreRoute: SearchMediaGenreRoute,
        searchMediaTagRoute: SearchMediaTagRoute,
        staffDetailsRoute: StaffDetailsRoute,
        studioMediasRoute: StudioMediasRoute,
        activityEntryProvider: ActivityEntryProvider<ActivityEntry, MediaCompactWithTagsEntry>,
        activitySortFilterViewModelProvider: @Composable () -> ActivitySortFilterViewModel,
        characterEntryProvider: CharacterEntryProvider<UserFavoritesCharactersQuery.Data.User.Favourites.Characters.Node, CharacterEntry, MediaWithListStatusEntry>,
        mediaWithListStatusEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaWithListStatusEntry>,
        mediaCompactWithTagsEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaCompactWithTagsEntry>,
        mediaPreviewWithDescriptionEntryProvider: MediaEntryProvider<MediaPreviewWithDescription, MediaPreviewWithDescriptionEntry>,
        staffEntryProvider: StaffEntryProvider<UserFavoritesStaffQuery.Data.User.Favourites.Staff.Node, StaffEntry, MediaWithListStatusEntry>,
        studioEntryProvider: StudioEntryProvider<StudioListRowFragment, StudioEntry, MediaWithListStatusEntry>,
        mediaHorizontalRow: LazyGridScope.(
            AniListViewer?,
            titleRes: StringResource,
            LazyPagingItems<MediaWithListStatusEntry>,
            viewAllRoute: NavDestination,
            viewAllContentDescriptionTextRes: StringResource,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        mediaHorizontalCardRow: LazyListScope.(
            AniListViewer?,
            List<MediaWithListStatusEntry?>,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        mediaViewOptionRow: @Composable (
            AniListViewer?,
            MediaViewOption,
            MediaPreviewWithDescriptionEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        charactersSection: LazyGridScope.(
            titleRes: StringResource,
            characters: LazyPagingItems<CharacterDetails>,
            viewAllRoute: (() -> NavDestination)?,
            viewAllContentDescriptionTextRes: StringResource,
        ) -> Unit,
        characterRow: @Composable (
            AniListViewer?,
            CharacterEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        staffSection: LazyGridScope.(
            titleRes: StringResource?,
            staff: LazyPagingItems<StaffDetails>,
            viewAllRoute: NavDestination,
            viewAllContentDescriptionTextRes: StringResource,
        ) -> Unit,
        staffRow: @Composable (
            AniListViewer?,
            StaffEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        studiosSection: LazyGridScope.(
            AniListViewer?,
            List<StudioEntry>,
            hasMore: Boolean,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        studioRow: @Composable (
            AniListViewer?,
            StudioEntry?,
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
    ) {
        navGraphBuilder.sharedElementComposable<User>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/user/{userId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/user/{userId}/.*" },
            ),
        ) {
            val destination = it.toRoute<User>()

            val activitySortFilterViewModel = activitySortFilterViewModelProvider()
            val viewModel = viewModel {
                component.aniListUserViewModelFactory(createSavedStateHandle())
                    .create(
                        activityEntryProvider = activityEntryProvider,
                        activitySortFilterViewModel = activitySortFilterViewModel,
                        mediaWithListStatusEntryProvider = mediaWithListStatusEntryProvider,
                        mediaCompactWithTagsEntryProvider = mediaCompactWithTagsEntryProvider,
                        studioEntryProvider = studioEntryProvider,
                    )
            }
            val entry by viewModel.entry.collectAsState()
            val headerValues = UserHeaderValues(
                params = destination.headerParams,
                user = { entry.result?.user },
            )
            val followingViewModel =
                viewModel { component.userSocialViewModelFollowing(viewModel.userId) }
            val followersViewModel =
                viewModel { component.userSocialViewModelFollowers(viewModel.userId) }
            val viewer by viewModel.viewer.collectAsState()
            val activities = viewModel.activities.collectAsLazyPagingItems()

            AniListViewerProfileScreen.UserScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                viewModel = viewModel,
                activitySortFilterViewModel = activitySortFilterViewModel,
                followingViewModel = followingViewModel,
                followersViewModel = followersViewModel,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                headerValues = headerValues,
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

        navGraphBuilder.sharedElementComposable<UserFollowing>(navigationTypeMap) {
            val destination = it.toRoute<UserFollowing>()
            val userFollowSortFilterViewModel = viewModel {
                component.userFollowSortFilterViewModel(createSavedStateHandle())
            }
            val viewModel = viewModel {
                component.userListViewModelFollowingFactory(
                    createSavedStateHandle(),
                    userFollowSortFilterViewModel,
                ).create(mediaWithListStatusEntryProvider)
            }
            val viewer by viewModel.viewer.collectAsState()
            UserListScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                sortFilterState = userFollowSortFilterViewModel.state,
                title = {
                    if (destination.userId == null) {
                        stringResource(Res.string.anime_user_following_you)
                    } else {
                        stringResource(
                            Res.string.anime_user_following_user,
                            destination.userName.orEmpty()
                        )
                    }
                },
                users = viewModel.users.collectAsLazyPagingItems(),
                mediaRow = { media, onClickListEdit ->
                    mediaHorizontalCardRow(viewer, media, onClickListEdit)
                }
            )
        }

        navGraphBuilder.sharedElementComposable<UserFollowers>(navigationTypeMap) {
            val destination = it.toRoute<UserFollowers>()
            val userFollowSortFilterViewModel = viewModel {
                component.userFollowSortFilterViewModel(createSavedStateHandle())
            }
            val viewModel = viewModel {
                component.userListViewModelFollowersFactory(
                    createSavedStateHandle(),
                    userFollowSortFilterViewModel
                ).create(mediaWithListStatusEntryProvider)
            }
            val viewer by viewModel.viewer.collectAsState()
            UserListScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                sortFilterState = userFollowSortFilterViewModel.state,
                title = {
                    if (destination.userId == null) {
                        stringResource(Res.string.anime_user_followers_you)
                    } else {
                        stringResource(
                            Res.string.anime_user_followers_user,
                            destination.userName.orEmpty()
                        )
                    }
                },
                users = viewModel.users.collectAsLazyPagingItems(),
                mediaRow = { media, onClickListEdit ->
                    mediaHorizontalCardRow(viewer, media, onClickListEdit)
                }
            )
        }

        navGraphBuilder.sharedElementComposable<UserFavoriteMedia>(
            navigationTypeMap
        ) {
            val destination = it.toRoute<UserFavoriteMedia>()
            val viewModel = viewModel {
                component.userFavoriteMediaViewModelFactory(createSavedStateHandle())
                    .create(mediaPreviewWithDescriptionEntryProvider)
            }
            val viewer by viewModel.viewer.collectAsState()
            val media = viewModel.media.collectAsLazyPagingItems()
            UserFavoriteMediaScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                onRefresh = viewModel::refresh,
                title = {
                    if (viewModel.mediaType == MediaType.ANIME) {
                        if (destination.userId == null) {
                            stringResource(Res.string.anime_user_favorite_anime_you)
                        } else {
                            stringResource(
                                Res.string.anime_user_favorite_anime_user,
                                destination.userName.orEmpty()
                            )
                        }
                    } else {
                        if (destination.userId == null) {
                            stringResource(Res.string.anime_user_favorite_manga_you)
                        } else {
                            stringResource(
                                Res.string.anime_user_favorite_manga_user,
                                destination.userName.orEmpty()
                            )
                        }
                    }
                },
                mediaViewOption = { viewModel.mediaViewOption },
                onMediaViewOptionChanged = { viewModel.mediaViewOption = it },
                media = media,
                mediaId = mediaPreviewWithDescriptionEntryProvider::id,
                mediaCell = { entry, onClickListEdit ->
                    mediaViewOptionRow(viewer, viewModel.mediaViewOption, entry, onClickListEdit)
                }
            )
        }

        navGraphBuilder.sharedElementComposable<UserFavoriteCharacters>(
            navigationTypeMap
        ) {
            val destination = it.toRoute<UserFavoriteCharacters>()
            val viewModel = viewModel {
                component.userFavoriteCharactersViewModelFactory(createSavedStateHandle())
                    .create(characterEntryProvider, mediaWithListStatusEntryProvider)
            }
            val viewer by viewModel.viewer.collectAsState()
            val characters = viewModel.characters.collectAsLazyPagingItems()
            UserFavoriteCharactersScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                onRefresh = viewModel::refresh,
                title = {
                    if (destination.userId == null) {
                        stringResource(Res.string.anime_user_favorite_characters_you)
                    } else {
                        stringResource(
                            Res.string.anime_user_favorite_characters_user,
                            destination.userName.orEmpty()
                        )
                    }
                },
                characters = characters,
                characterId = characterEntryProvider::id,
                characterRow = { entry, onClickListEdit ->
                    characterRow(viewer, entry, onClickListEdit)
                }
            )
        }

        navGraphBuilder.sharedElementComposable<UserFavoriteStaff>(
            navigationTypeMap
        ) {
            val destination = it.toRoute<UserFavoriteStaff>()
            val viewModel = viewModel {
                component.userFavoriteStaffViewModelFactory(createSavedStateHandle())
                    .create(staffEntryProvider, mediaWithListStatusEntryProvider)
            }
            val viewer by viewModel.viewer.collectAsState()
            val staff = viewModel.staff.collectAsLazyPagingItems()
            UserFavoriteStaffScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                onRefresh = viewModel::refresh,
                title = {
                    if (destination.userId == null) {
                        stringResource(Res.string.anime_user_favorite_staff_you)
                    } else {
                        stringResource(
                            Res.string.anime_user_favorite_staff_user,
                            destination.userName.orEmpty()
                        )
                    }
                },
                staff = staff,
                staffId = staffEntryProvider::id,
                staffRow = { entry, onClickListEdit ->
                    staffRow(viewer, entry, onClickListEdit)
                }
            )
        }

        navGraphBuilder.sharedElementComposable<UserFavoriteStudios>(
            navigationTypeMap
        ) {
            val destination = it.toRoute<UserFavoriteStudios>()
            val viewModel = viewModel {
                component.userFavoriteStudiosViewModelFactory(createSavedStateHandle())
                    .create(mediaWithListStatusEntryProvider, studioEntryProvider)
            }
            val viewer by viewModel.viewer.collectAsState()
            val studios = viewModel.studios.collectAsLazyPagingItems()
            UserFavoriteStudiosScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                onRefresh = viewModel::refresh,
                title = {
                    if (destination.userId == null) {
                        stringResource(Res.string.anime_user_favorite_studios_you)
                    } else {
                        stringResource(
                            Res.string.anime_user_favorite_studios_user,
                            destination.userName.orEmpty(),
                        )
                    }
                },
                studios = studios,
                studioId = studioEntryProvider::id,
                studioRow = { entry, onClickListEdit ->
                    studioRow(viewer, entry, onClickListEdit)
                }
            )
        }
    }
}
