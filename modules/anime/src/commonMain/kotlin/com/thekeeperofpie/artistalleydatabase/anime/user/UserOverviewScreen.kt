package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_anime_label
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_characters_label
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_manga_label
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_media_view_all_anime_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_media_view_all_character_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_media_view_all_manga_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_media_view_all_staff_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_media_view_all_studios_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_staff_label
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_studios_label
import artistalleydatabase.modules.anime.generated.resources.anime_user_following
import artistalleydatabase.modules.anime.generated.resources.anime_user_following_you
import artistalleydatabase.modules.anime.generated.resources.anime_user_is_follower_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_user_is_following_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_user_is_not_follower_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_user_is_not_following_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_user_previous_names_label
import artistalleydatabase.modules.utils_compose.generated.resources.view_all
import com.anilist.UserByIdQuery
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.character.charactersSection
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.mediaHorizontalRow
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioListRow
import com.thekeeperofpie.artistalleydatabase.anime.ui.DescriptionSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeResourceUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
object UserOverviewScreen {

    @Composable
    operator fun invoke(
        userId: String?,
        entry: AniListUserScreen.Entry,
        viewModel: AniListUserViewModel,
        editViewModel: MediaEditViewModel,
        viewer: AniListViewer?,
        isFollowing: @Composable () -> Boolean,
        onFollowingClick: () -> Unit,
        modifier: Modifier = Modifier,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val user = entry.user
        var descriptionExpanded by remember { mutableStateOf(false) }
        val anime = viewModel.anime.collectAsLazyPagingItems()
        val manga = viewModel.manga.collectAsLazyPagingItems()
        val characters = viewModel.characters.collectAsLazyPagingItems()
        val staff = viewModel.staff.collectAsLazyPagingItems()
        val navigationCallback = LocalNavigationCallback.current
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

            // TODO: mediaListEntry doesn't load properly for these, figure out a way to show status
            mediaHorizontalRow(
                viewer = viewer,
                editViewModel = editViewModel,
                titleRes = Res.string.anime_user_favorite_anime_label,
                entries = anime,
                forceListEditIcon = true,
                onClickViewAll = {
                    navigationCallback.navigate(
                        AnimeDestination.UserFavoriteMedia(
                            userId = userId,
                            userName = user.name,
                            mediaType = MediaType.ANIME,
                        )
                    )
                },
                viewAllContentDescriptionTextRes = Res.string.anime_user_favorite_media_view_all_anime_content_description,
            )

            mediaHorizontalRow(
                viewer = viewer,
                editViewModel = editViewModel,
                titleRes = Res.string.anime_user_favorite_manga_label,
                entries = manga,
                forceListEditIcon = true,
                onClickViewAll = {
                    navigationCallback.navigate(
                        AnimeDestination.UserFavoriteMedia(
                            userId = userId,
                            userName = user.name,
                            mediaType = MediaType.MANGA,
                        )
                    )
                },
                viewAllContentDescriptionTextRes = Res.string.anime_user_favorite_media_view_all_manga_content_description,
            )

            charactersSection(
                titleRes = Res.string.anime_user_favorite_characters_label,
                characters = characters,
                onClickViewAll = {
                    it.navigate(
                        AnimeDestination.UserFavoriteCharacters(
                            userId = userId,
                            userName = user.name,
                        )
                    )
                },
                viewAllContentDescriptionTextRes = Res.string.anime_user_favorite_media_view_all_character_content_description,
            )

            staffSection(
                titleRes = Res.string.anime_user_favorite_staff_label,
                staffList = staff,
                onClickViewAll = {
                    it.navigate(
                        AnimeDestination.UserFavoriteStaff(
                            userId = userId,
                            userName = user.name,
                        )
                    )
                },
                viewAllContentDescriptionTextRes = Res.string.anime_user_favorite_media_view_all_staff_content_description,
            )

            val studios = viewModel.studios
            favoriteStudiosSection(
                viewer = viewer,
                editViewModel = editViewModel,
                studios = studios.studios,
                hasMore = studios.hasMore,
                onClickViewAll = {
                    it.navigate(
                        AnimeDestination.UserFavoriteStudios(
                            userId = userId,
                            userName = user.name
                        )
                    )
                },
                viewAllContentDescriptionTextRes = Res.string.anime_user_favorite_media_view_all_studios_content_description,
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

    private fun LazyGridScope.favoriteStudiosSection(
        viewer: AniListViewer?,
        editViewModel: MediaEditViewModel,
        studios: List<StudioListRow.Entry>,
        hasMore: Boolean,
        onClickViewAll: ((AnimeNavigator.NavigationCallback) -> Unit)? = null,
        viewAllContentDescriptionTextRes: StringResource? = null,
    ) {
        if (studios.isEmpty()) return
        item("favoriteStudiosHeader") {
            val navigationCallback = LocalNavigationCallback.current
            DetailsSectionHeader(
                stringResource(Res.string.anime_user_favorite_studios_label),
                onClickViewAll = onClickViewAll?.let { { it(navigationCallback) } },
                viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
            )
        }

        itemsIndexed(
            items = studios,
            key = { _, item -> item.studio.id },
            contentType = { _, _ -> "studio" },
        ) { index, item ->
            SharedTransitionKeyScope("user_favorite_studio_row", item.studio.id.toString()) {
                StudioListRow(
                    viewer = viewer,
                    entry = item,
                    onClickListEdit = editViewModel::initialize,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = if (index == studios.lastIndex && !hasMore) 0.dp else 16.dp,
                        ),
                    mediaWidth = 64.dp,
                    mediaHeight = 96.dp
                )
            }
        }

        if (hasMore) {
            item("favoriteStudios-showAll") {
                val navigationCallback = LocalNavigationCallback.current
                ElevatedCard(
                    onClick = { onClickViewAll?.invoke(navigationCallback) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .animateItem()
                ) {
                    Text(
                        text = ComposeResourceUtils.stringResource(UtilsStrings.view_all),
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
}
