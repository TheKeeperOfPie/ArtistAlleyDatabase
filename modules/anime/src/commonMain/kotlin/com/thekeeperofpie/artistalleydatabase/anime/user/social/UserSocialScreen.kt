package com.thekeeperofpie.artistalleydatabase.anime.user.social

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_user_social_followers
import artistalleydatabase.modules.anime.generated.resources.anime_user_social_following
import artistalleydatabase.modules.anime.generated.resources.anime_user_social_not_following_anyone
import coil3.annotation.ExperimentalCoilApi
import com.anilist.data.fragment.UserNavigationData
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.anime.user.UserHeaderParams
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalCoilApi::class)
object UserSocialScreen {

    private val USER_IMAGE_SIZE = 120.dp

    @Composable
    operator fun invoke(
        userId: String?,
        user: UserNavigationData?,
        bottomNavigationState: BottomNavigationState?,
    ) {
        val animeComponent = LocalAnimeComponent.current
        val followingViewModel = viewModel { animeComponent.userSocialViewModelFollowing(userId) }
        val followersViewModel = viewModel { animeComponent.userSocialViewModelFollowers(userId) }

        // TODO: Handle LoadStates
        val following = followingViewModel.data().collectAsLazyPagingItems()
        val followers = followersViewModel.data().collectAsLazyPagingItems()

        val isLoading = following.loadState.refresh is LoadState.Loading
                && followers.loadState.refresh is LoadState.Loading
        val navigationCallback = LocalNavigationCallback.current
        LazyColumn(
            contentPadding = PaddingValues(
                bottom = 16.dp + (bottomNavigationState?.bottomNavBarPadding() ?: 0.dp),
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLoading) {
                item("loading") {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp)
                        )
                    }
                }
                return@LazyColumn
            }

            usersRow(
                key = "following",
                data = following,
                titleRes = Res.string.anime_user_social_following,
                emptyTextRes = Res.string.anime_user_social_not_following_anyone,
                onClickViewAll = {
                    navigationCallback.navigate(
                        AnimeDestination.UserFollowing(
                            userId = userId,
                            userName = user?.name,
                        )
                    )
                },
            )

            if (followers.itemCount > 0) {
                usersRow(
                    key = "followers",
                    data = followers,
                    titleRes = Res.string.anime_user_social_followers,
                    emptyTextRes = null,
                    onClickViewAll = {
                        navigationCallback.navigate(
                            AnimeDestination.UserFollowers(
                                userId = userId,
                                userName = user?.name,
                            )
                        )
                    },
                )
            }
        }
    }

    private fun LazyListScope.usersRow(
        key: String,
        data: LazyPagingItems<out UserNavigationData>,
        titleRes: StringResource,
        emptyTextRes: StringResource?,
        onClickViewAll: () -> Unit,
    ) {
        item("header_$key") {
            DetailsSectionHeader(text = stringResource(titleRes), onClickViewAll = onClickViewAll)
        }

        val empty = data.itemCount == 0
        if (emptyTextRes == null && empty) {
            return
        }

        item(key) {
            if (empty) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(emptyTextRes!!),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .align(Alignment.TopCenter)
                    )
                }
            } else {
                SharedTransitionKeyScope("user_social_row", key) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            count = data.itemCount,
                            key = data.itemKey { it.id },
                            contentType = data.itemContentType { "user" },
                        ) {
                            UserPreview(
                                user = data[it],
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun UserPreview(user: UserNavigationData?) {
        val navigationCallback = LocalNavigationCallback.current
        val imageState = rememberCoilImageState(user?.avatar?.large)
        val sharedTransitionKey = user?.id?.toString()
            ?.let { SharedTransitionKey.makeKeyForId(it) }
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .size(USER_IMAGE_SIZE)
                .clickable {
                    user?.let {
                        navigationCallback.navigate(
                            AnimeDestination.User(
                                userId = it.id.toString(),
                                sharedTransitionKey = sharedTransitionKey,
                                headerParams = UserHeaderParams(
                                    name = it.name,
                                    bannerImage = null,
                                    coverImage = imageState.toImageState(),
                                )
                            )
                        )
                    }
                }
        ) {
            Box {
                UserAvatarImage(
                    imageState = imageState,
                    image = imageState.request().build(),
                    modifier = Modifier
                        .size(USER_IMAGE_SIZE)
                        .sharedElement(sharedTransitionKey, "user_image")
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .placeholder(
                            visible = user == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )

                Text(
                    text = user?.name ?: "USERNAME",
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                        .placeholder(
                            visible = user == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}
