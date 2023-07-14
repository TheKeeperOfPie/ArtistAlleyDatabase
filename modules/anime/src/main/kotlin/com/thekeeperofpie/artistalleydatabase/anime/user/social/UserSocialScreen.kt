package com.thekeeperofpie.artistalleydatabase.anime.user.social

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.fragment.UserNavigationData
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

object UserSocialScreen {

    private val USER_IMAGE_SIZE = 120.dp

    @Composable
    operator fun invoke(
        userId: String?,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
        bottomNavigationState: BottomNavigationState?,
    ) {
        val followingViewModel = hiltViewModel<UserSocialViewModel.Following>()
            .apply { initialize(userId) }
        val followersViewModel = hiltViewModel<UserSocialViewModel.Followers>()
            .apply { initialize(userId) }
        
        // TODO: Handle LoadStates
        val following = followingViewModel.data().collectAsLazyPagingItems()
        val followers = followersViewModel.data().collectAsLazyPagingItems()

        val isLoading = following.loadState.refresh is LoadState.Loading
                && followers.loadState.refresh is LoadState.Loading
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
                titleRes = R.string.anime_user_social_following,
                emptyTextRes = R.string.anime_user_social_not_following_anyone,
                colorCalculationState = colorCalculationState,
                navigationCallback = navigationCallback,
            )

            if (followers.itemCount > 0) {
                usersRow(
                    key = "followers",
                    data = followers,
                    titleRes = R.string.anime_user_social_followers,
                    emptyTextRes = null,
                    colorCalculationState = colorCalculationState,
                    navigationCallback = navigationCallback,
                )
            }
        }
    }

    private fun LazyListScope.usersRow(
        key: String,
        data: LazyPagingItems<out UserNavigationData>,
        @StringRes titleRes: Int,
        @StringRes emptyTextRes: Int?,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        item("header_$key") {
            DetailsSectionHeader(text = stringResource(titleRes))
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
                            colorCalculationState = colorCalculationState,
                            navigationCallback = navigationCallback,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun UserPreview(
        user: UserNavigationData?,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        var imageWidthToHeightRatio by remember { MutableSingle(1f) }
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .size(USER_IMAGE_SIZE)
                .clickable {
                    user?.let {
                        navigationCallback.onUserClick(it, imageWidthToHeightRatio)
                    }
                }
        ) {
            Box {
                val dimension =
                    Dimension.Pixels(LocalDensity.current.run { USER_IMAGE_SIZE.roundToPx() })
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user?.avatar?.large)
                        .crossfade(true)
                        .allowHardware(colorCalculationState.hasColor(user?.id.toString()))
                        .size(width = dimension, height = dimension)
                        .build(),
                    contentScale = ContentScale.Crop,
                    fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                    onSuccess = {
                        imageWidthToHeightRatio = it.widthToHeightRatio()
                        user ?: return@AsyncImage
                        ComposeColorUtils.calculatePalette(
                            user.id.toString(),
                            it,
                            colorCalculationState,
                        )
                    },
                    contentDescription = stringResource(R.string.anime_user_image),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .fillMaxHeight()
                        .size(USER_IMAGE_SIZE)
                        .placeholder(
                            visible = user == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
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
