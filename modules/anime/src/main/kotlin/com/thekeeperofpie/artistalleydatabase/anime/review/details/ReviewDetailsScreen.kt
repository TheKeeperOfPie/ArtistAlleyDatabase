package com.thekeeperofpie.artistalleydatabase.anime.review.details

import android.text.format.DateUtils
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anilist.ReviewDetailsQuery
import com.anilist.type.ReviewRating
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewRatingIconsSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsLoadingOrError
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.ImageHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
object ReviewDetailsScreen {

    private val SCREEN_KEY = AnimeNavDestinations.REVIEW_DETAILS.id

    @Composable
    operator fun invoke(
        viewModel: ReviewDetailsViewModel,
        upIconOption: UpIconOption,
        headerValues: MediaHeaderValues,
    ) {
        val entry = viewModel.entry
        val media = entry?.review?.media
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
        Scaffold(
            topBar = {
                CollapsingToolbar(
                    maxHeight = 356.dp,
                    pinnedHeight = 120.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    val navigationCallback = LocalNavigationCallback.current
                    var coverImageWidthToHeightRatio by remember { mutableFloatStateOf(1f) }
                    val title = entry?.review?.media?.title?.primaryTitle()
                    MediaHeader(
                        screenKey = SCREEN_KEY,
                        upIconOption = upIconOption,
                        mediaId = entry?.review?.media?.id?.toString(),
                        mediaType = viewModel.entry?.review?.media?.type,
                        titles = entry?.titlesUnique,
                        episodes = media?.episodes,
                        format = media?.format,
                        averageScore = media?.averageScore,
                        popularity = media?.popularity,
                        progress = it,
                        headerValues = headerValues,
                        onFavoriteChanged = {
                            viewModel.favoritesToggleHelper.set(
                                headerValues.type.toFavoriteType(),
                                entry?.review?.media?.id.toString(),
                                it,
                            )
                        },
                        onImageWidthToHeightRatioAvailable = {
                            coverImageWidthToHeightRatio = it
                        },
                        enableCoverImageSharedElement = false,
                        onCoverImageClick = {
                            entry?.review?.media?.let { media ->
                                navigationCallback.navigate(
                                    AnimeDestinations.MediaDetails(
                                        mediaId = media.id.toString(),
                                        title = title,
                                        coverImage = media.coverImage?.extraLarge,
                                        headerParams = MediaHeaderParams(
                                            title = title,
                                            coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
                                            media = media,
                                        )
                                    )
                                )
                            }
                        }
                    )
                }
            },
            snackbarHost = {
                val error = viewModel.error
                SnackbarErrorText(
                    error?.first,
                    error?.second,
                    onErrorDismiss = { viewModel.error = null }
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                val review = entry?.review
                if (review == null) {
                    DetailsLoadingOrError(
                        loading = viewModel.loading,
                        errorResource = { viewModel.error },
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        var userImageWidthToHeightRatio by remember { MutableSingle(1f) }
                        val navigationCallback = LocalNavigationCallback.current
                        val shape = RoundedCornerShape(12.dp)
                        UserAvatarImage(
                            screenKey = SCREEN_KEY,
                            userId = review.user?.id?.toString(),
                            image = review.user?.avatar?.large,
                            contentScale = ContentScale.FillHeight,
                            contentDescriptionTextRes = R.string.anime_media_details_reviews_user_avatar_content_description,
                            onSuccess = { userImageWidthToHeightRatio = it.widthToHeightRatio() },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(shape)
                                .border(width = Dp.Hairline, MaterialTheme.colorScheme.primary, shape)
                                .clickable {
                                    review.user?.let {
                                        navigationCallback.onUserClick(
                                            it,
                                            userImageWidthToHeightRatio
                                        )
                                    }
                                },
                        )

                        AutoSizeText(
                            text = review.user?.name.orEmpty(),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        )

                        ReviewRatingIconsSection(
                            score = review.score,
                            rating = review.rating,
                            ratingAmount = review.ratingAmount,
                        )
                    }

                    val timestamp = remember(review) {
                        DateUtils.getRelativeTimeSpanString(
                            review.createdAt * 1000L,
                            Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond() * 1000,
                            0,
                            DateUtils.FORMAT_ABBREV_ALL,
                        )
                    }

                    Text(
                        text = timestamp.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    ImageHtmlText(
                        text = review.body.orEmpty(),
                        color = MaterialTheme.typography.bodyMedium.color
                            .takeOrElse { LocalContentColor.current },
                        modifier = Modifier.padding(16.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
                    ) {
                        val ratingColor = review.score?.let { MediaUtils.ratingColor(it) }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            ElevatedCard(
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = ratingColor
                                        ?: MaterialTheme.colorScheme.surface,
                                ),
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.anime_review_details_score,
                                        review.score?.toString() ?: "-"
                                    ),
                                    color = ratingColor?.let(ComposeColorUtils::bestTextColor)
                                        ?: Color.Unspecified,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 10.dp
                                    )
                                )
                            }
                        }

                        if (viewModel.viewer.collectAsState().value != null) {
                            val userRating = viewModel.userRating
                            val isUpvoted = userRating == ReviewRating.UP_VOTE
                            IconButton(onClick = {
                                viewModel.onUserRating(upvote = true.takeUnless { isUpvoted })
                            }) {
                                Icon(
                                    imageVector = if (isUpvoted) {
                                        Icons.Filled.ThumbUp
                                    } else {
                                        Icons.Outlined.ThumbUp
                                    },
                                    contentDescription = stringResource(
                                        R.string.anime_review_details_upvote_content_description
                                    )
                                )
                            }

                            val isDownvoted = userRating == ReviewRating.DOWN_VOTE
                            IconButton(onClick = {
                                viewModel.onUserRating(upvote = false.takeUnless { isDownvoted })
                            }) {
                                Icon(
                                    imageVector = if (isDownvoted) {
                                        Icons.Filled.ThumbDown
                                    } else {
                                        Icons.Outlined.ThumbDown
                                    },
                                    tint = if (isDownvoted) Color.Red else LocalContentColor.current,
                                    contentDescription = stringResource(
                                        R.string.anime_review_details_downvote_content_description
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    data class Entry(
        val review: ReviewDetailsQuery.Data.Review,
    ) {
        val titlesUnique = review.media?.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()

        val color = review.media?.coverImage?.color?.let(ComposeColorUtils::hexToColor)
    }
}
