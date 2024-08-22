package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import artistalleydatabase.modules.utils_compose.generated.resources.view_all
import com.anilist.UserByIdQuery
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.charactersSection
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.mediaHorizontalRow
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioListRow
import com.thekeeperofpie.artistalleydatabase.anime.ui.DescriptionSection
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeResourceUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings

@OptIn(
    ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
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
        LazyColumn(
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

            if (!entry.about?.value.isNullOrEmpty()) {
                item("aboutSection", "aboutSection") {
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
                titleRes = R.string.anime_user_favorite_anime_label,
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
                viewAllContentDescriptionTextRes = R.string.anime_user_favorite_media_view_all_anime_content_description,
            )

            mediaHorizontalRow(
                viewer = viewer,
                editViewModel = editViewModel,
                titleRes = R.string.anime_user_favorite_manga_label,
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
                viewAllContentDescriptionTextRes = R.string.anime_user_favorite_media_view_all_manga_content_description,
            )

            charactersSection(
                titleRes = R.string.anime_user_favorite_characters_label,
                characters = characters,
                onClickViewAll = {
                    it.navigate(
                        AnimeDestination.UserFavoriteCharacters(
                            userId = userId,
                            userName = user.name,
                        )
                    )
                },
                viewAllContentDescriptionTextRes = R.string.anime_user_favorite_media_view_all_character_content_description,
            )

            staffSection(
                titleRes = R.string.anime_user_favorite_staff_label,
                staffList = staff,
                onClickViewAll = {
                    it.navigate(
                        AnimeDestination.UserFavoriteStaff(
                            userId = userId,
                            userName = user.name,
                        )
                    )
                },
                viewAllContentDescriptionTextRes = R.string.anime_user_favorite_media_view_all_staff_content_description,
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
                viewAllContentDescriptionTextRes = R.string.anime_user_favorite_media_view_all_studios_content_description,
            )

            previousNamesSection(
                names = user.previousNames?.filterNotNull()?.mapNotNull { it.name }.orEmpty()
            )
        }
    }

    private fun LazyListScope.followingSection(
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
                                    R.string.anime_user_is_follower_content_description
                                } else {
                                    R.string.anime_user_is_not_follower_content_description
                                }
                            ),
                        )
                    },
                    label = { AutoHeightText(stringResource(R.string.anime_user_following_you)) },
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
                                    R.string.anime_user_is_following_content_description
                                } else {
                                    R.string.anime_user_is_not_following_content_description
                                }
                            ),
                        )
                    },
                    label = { AutoHeightText(stringResource(R.string.anime_user_following)) },
                )
            }
        }
    }

    private fun LazyListScope.favoriteStudiosSection(
        viewer: AniListViewer?,
        editViewModel: MediaEditViewModel,
        studios: List<StudioListRow.Entry>,
        hasMore: Boolean,
        onClickViewAll: ((AnimeNavigator.NavigationCallback) -> Unit)? = null,
        @StringRes viewAllContentDescriptionTextRes: Int? = null,
    ) {
        if (studios.isEmpty()) return
        item("favoriteStudiosHeader") {
            val navigationCallback = LocalNavigationCallback.current
            DetailsSectionHeader(
                stringResource(R.string.anime_user_favorite_studios_label),
                onClickViewAll = onClickViewAll?.let { { it(navigationCallback) } },
                viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
            )
        }

        itemsIndexed(
            items = studios,
            key = { index, item -> item.studio.id },
            contentType = { _, _ -> "studio" },
        ) { index, item ->
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

    private fun LazyListScope.previousNamesSection(names: List<String>) {
        if (names.isEmpty()) return
        item {
            DetailsSectionHeader(stringResource(R.string.anime_user_previous_names_label))
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
