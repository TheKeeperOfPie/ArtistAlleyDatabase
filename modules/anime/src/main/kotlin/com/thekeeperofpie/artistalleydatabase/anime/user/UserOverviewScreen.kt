package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.AuthedUserQuery
import com.anilist.UserByIdQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.charactersSection
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.mediaHorizontalRow
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioListRow
import com.thekeeperofpie.artistalleydatabase.anime.ui.descriptionSection
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
object UserOverviewScreen {

    @Composable
    operator fun invoke(
        entry: AniListUserScreen.Entry,
        viewModel: AniListUserViewModel,
        viewer: AuthedUserQuery.Data.Viewer?,
        isFollowing: @Composable () -> Boolean,
        onFollowingClick: () -> Unit,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
        modifier: Modifier = Modifier,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val user = entry.user
        var descriptionExpanded by remember { mutableStateOf(false) }
        val anime = viewModel.anime.collectAsLazyPagingItems()
        val manga = viewModel.manga.collectAsLazyPagingItems()
        val characters = viewModel.characters.collectAsLazyPagingItems()
        val staff = viewModel.staff.collectAsLazyPagingItems()
        val studios = viewModel.studios.collectAsLazyPagingItems()
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

            descriptionSection(
                markdownText = user.about?.trim(),
                expanded = { descriptionExpanded },
                onExpandedChange = { descriptionExpanded = it },
                imagesSupported = true,
            )

            mediaHorizontalRow(
                screenKey = viewModel.screenKey,
                titleRes = R.string.anime_user_favorite_anime_label,
                entries = anime,
                onClickEntry = navigationCallback::onMediaClick,
                colorCalculationState = colorCalculationState,
            )

            mediaHorizontalRow(
                screenKey = viewModel.screenKey,
                titleRes = R.string.anime_user_favorite_manga_label,
                entries = manga,
                onClickEntry = navigationCallback::onMediaClick,
                colorCalculationState = colorCalculationState,
            )

            charactersSection(
                screenKey = viewModel.screenKey,
                titleRes = R.string.anime_user_favorite_characters_label,
                characters = characters,
                onCharacterClick = navigationCallback::onCharacterClick,
                onCharacterLongClick = navigationCallback::onCharacterLongClick,
                onStaffClick = navigationCallback::onStaffClick,
                colorCalculationState = colorCalculationState,
            )

            staffSection(
                screenKey = viewModel.screenKey,
                titleRes = R.string.anime_user_favorite_staff_label,
                staffList = staff,
                onStaffClick = navigationCallback::onStaffClick,
                onStaffLongClick = navigationCallback::onStaffLongClick,
                colorCalculationState = colorCalculationState,
            )

            favoriteStudiosSection(
                screenKey = viewModel.screenKey,
                studios = studios,
                navigationCallback = navigationCallback,
            )

            previousNamesSection(
                names = user.previousNames?.filterNotNull()?.mapNotNull { it.name }.orEmpty()
            )
        }
    }

    private fun LazyListScope.followingSection(
        user: UserByIdQuery.Data.User,
        viewer: AuthedUserQuery.Data.Viewer?,
        isFollowing: @Composable () -> Boolean,
        onFollowingClick: () -> Unit,
    ) {
        if (user.id == viewer?.id) return
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
        screenKey: String,
        studios: LazyPagingItems<StudioListRow.Entry>,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        if (studios.itemCount == 0) return
        item {
            DetailsSectionHeader(stringResource(R.string.anime_user_favorite_studios_label))
        }

        items(
            count = studios.itemCount,
            key = studios.itemKey { it.studio.id },
            contentType = studios.itemContentType { "studio" },
        ) {
            val studio = studios[it]
            StudioListRow(
                screenKey = screenKey,
                entry = studio,
                navigationCallback = navigationCallback,
                mediaWidth = 64.dp,
                mediaHeight = 96.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (it == studios.itemCount - 1) 0.dp else 16.dp,
                    )
            )
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
                        Divider()
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
