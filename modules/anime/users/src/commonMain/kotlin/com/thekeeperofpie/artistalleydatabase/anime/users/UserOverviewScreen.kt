package com.thekeeperofpie.artistalleydatabase.anime.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.users.generated.resources.Res
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_anime_label
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_characters_label
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_manga_label
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_media_view_all_anime_content_description
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_media_view_all_character_content_description
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_media_view_all_manga_content_description
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_media_view_all_staff_content_description
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_media_view_all_studios_content_description
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_staff_label
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_studios_label
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_following
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_following_you
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_is_follower_content_description
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_is_following_content_description
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_is_not_follower_content_description
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_is_not_following_content_description
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_previous_names_label
import artistalleydatabase.modules.utils_compose.generated.resources.view_all
import com.anilist.data.UserByIdQuery
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterDetails
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffDetails
import com.thekeeperofpie.artistalleydatabase.anime.ui.DescriptionSection
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
object UserOverviewScreen {

    @Composable
    operator fun <MediaEntry : Any, StudioEntry : Any> invoke(
        userId: String?,
        entry: Entry,
        anime: LazyPagingItems<MediaEntry>,
        manga: LazyPagingItems<MediaEntry>,
        characters: LazyPagingItems<CharacterDetails>,
        staff: LazyPagingItems<StaffDetails>,
        studios: () -> LoadingResult<UserStudiosEntry<StudioEntry>>,
        viewer: AniListViewer?,
        isFollowing: @Composable () -> Boolean,
        onFollowingClick: () -> Unit,
        mediaRow: LazyGridScope.(
            titleRes: StringResource,
            LazyPagingItems<MediaEntry>,
            viewAllRoute: NavDestination,
            viewAllContentDescriptionTextRes: StringResource,
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
        studiosSection: LazyGridScope.(List<StudioEntry>, hasMore: Boolean) -> Unit,
        modifier: Modifier = Modifier,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val user = entry.user
        var descriptionExpanded by remember { mutableStateOf(false) }
        val navigationController = LocalNavigationController.current
        LazyVerticalGrid(
            columns = GridUtils.standardWidthAdaptiveCells,
            contentPadding = PaddingValues(
                bottom = 16.dp + (bottomNavigationState?.bottomNavBarPadding() ?: 0.dp)
            ),
            modifier = modifier.fillMaxSize()
        ) {
            followingSection(
                user = user,
                viewer = viewer,
                isFollowing = isFollowing,
                onFollowingClick = onFollowingClick,
            )

            if (entry.about != null) {
                item(
                    key = "aboutSection",
                    span = GridUtils.maxSpanFunction,
                    contentType = "aboutSection",
                ) {
                    DescriptionSection(
                        markdownText = entry.about,
                        expanded = { descriptionExpanded },
                        onExpandedChange = { descriptionExpanded = it },
                    )
                }
            }

            mediaRow(
                Res.string.anime_user_favorite_anime_label,
                anime,
                UserDestinations.UserFavoriteMedia(
                    userId = userId,
                    userName = user.name,
                    mediaType = MediaType.ANIME,
                ),
                Res.string.anime_user_favorite_media_view_all_anime_content_description
            )
            mediaRow(
                Res.string.anime_user_favorite_manga_label,
                manga,
                UserDestinations.UserFavoriteMedia(
                    userId = userId,
                    userName = user.name,
                    mediaType = MediaType.MANGA,
                ),
                Res.string.anime_user_favorite_media_view_all_manga_content_description
            )

            charactersSection(
                Res.string.anime_user_favorite_characters_label,
                characters,
                {
                    UserDestinations.UserFavoriteCharacters(
                        userId = userId,
                        userName = user.name,
                    )
                },
                Res.string.anime_user_favorite_media_view_all_character_content_description,
            )

            staffSection(
                Res.string.anime_user_favorite_staff_label,
                staff,
                UserDestinations.UserFavoriteStaff(
                    userId = userId,
                    userName = user.name,
                ),
                Res.string.anime_user_favorite_media_view_all_staff_content_description,
            )

            favoriteStudiosSection(
                studios = studios,
                onClickViewAll = {
                    navigationController.navigate(
                        UserDestinations.UserFavoriteStudios(
                            userId = userId,
                            userName = user.name
                        )
                    )
                },
                viewAllContentDescriptionTextRes = Res.string.anime_user_favorite_media_view_all_studios_content_description,
                studiosSection = studiosSection,
            )

            previousNamesSection(
                names = user.previousNames?.filterNotNull()?.mapNotNull { it.name }.orEmpty()
            )
        }
    }

    private fun LazyGridScope.followingSection(
        user: UserByIdQuery.Data.User,
        viewer: AniListViewer?,
        isFollowing: @Composable () -> Boolean,
        onFollowingClick: () -> Unit,
    ) {
        if (user.id.toString() == viewer?.id) return
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp)
            ) {
                FilterChip(
                    onClick = { /*TODO*/ },
                    selected = user.isFollower == true,
                    leadingIcon = {
                        Icon(
                            imageVector = if (user.isFollower == true) {
                                Icons.Filled.Check
                            } else {
                                Icons.Filled.Close
                            },
                            contentDescription = stringResource(
                                if (user.isFollower == true) {
                                    Res.string.anime_user_is_follower_content_description
                                } else {
                                    Res.string.anime_user_is_not_follower_content_description
                                }
                            ),
                        )
                    },
                    label = { AutoHeightText(stringResource(Res.string.anime_user_following_you)) },
                )

                val following = isFollowing()
                FilterChip(
                    onClick = onFollowingClick,
                    selected = following,
                    leadingIcon = {
                        Icon(
                            imageVector = if (following) {
                                Icons.Filled.Check
                            } else {
                                Icons.Filled.Close
                            },
                            contentDescription = stringResource(
                                if (following) {
                                    Res.string.anime_user_is_following_content_description
                                } else {
                                    Res.string.anime_user_is_not_following_content_description
                                }
                            ),
                        )
                    },
                    label = { AutoHeightText(stringResource(Res.string.anime_user_following)) },
                )
            }
        }
    }

    private fun <StudioEntry> LazyGridScope.favoriteStudiosSection(
        studios: () -> LoadingResult<UserStudiosEntry<StudioEntry>>,
        studiosSection: LazyGridScope.(List<StudioEntry>, hasMore: Boolean) -> Unit,
        onClickViewAll: (() -> Unit)? = null,
        viewAllContentDescriptionTextRes: StringResource? = null,
    ) {
        val studiosLoadingResult = studios()
        val studiosEntry = studiosLoadingResult.result
        val studios = studiosEntry?.studios
        if (studios.isNullOrEmpty()) return
        item("favoriteStudiosHeader") {
            DetailsSectionHeader(
                stringResource(Res.string.anime_user_favorite_studios_label),
                onClickViewAll = onClickViewAll,
                viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
            )
        }

        studiosSection(studios, studiosEntry.hasMore)

        if (studiosEntry.hasMore) {
            item("favoriteStudios-showAll") {
                ElevatedCard(
                    onClick = { onClickViewAll?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .animateItem()
                ) {
                    Text(
                        text = stringResource(UtilsStrings.view_all),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

    private fun LazyGridScope.previousNamesSection(names: List<String>) {
        if (names.isEmpty()) return
        item {
            DetailsSectionHeader(stringResource(Res.string.anime_user_previous_names_label))
        }

        item {
            ElevatedCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                names.forEachIndexed { index, name ->
                    if (index != 0) {
                        HorizontalDivider()
                    }

                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }

    interface Entry {
        val user: UserByIdQuery.Data.User
        val about: MarkdownText?
    }
}
