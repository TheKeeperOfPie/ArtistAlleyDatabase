@file:OptIn(ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.review

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.ThumbDownAlt
import androidx.compose.material.icons.filled.ThumbUpAlt
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anilist.MediaDetails2Query
import com.anilist.fragment.MediaAndReviewsReview
import com.anilist.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSection
import com.thekeeperofpie.artistalleydatabase.compose.CoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.recomposeHighlighter
import com.thekeeperofpie.artistalleydatabase.compose.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.SharedTransitionKeyScope
import java.time.Instant
import java.time.ZoneOffset

object ReviewComposables  {
    const val REVIEWS_ABOVE_FOLD = 3
}

@Composable
fun ReviewSmallCard(
    screenKey: String,
    review: MediaAndReviewsReview?,
    onClick: (AnimeNavigator.NavigationCallback) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationCallback = LocalNavigationCallback.current
    ElevatedCard(
        modifier = modifier
            .clickable(
                enabled = review?.id != null,
                onClick = { onClick(navigationCallback) },
            ),
    ) {
        ReviewSmallCardContent(screenKey, review)
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.ReviewSmallCardContent(
    screenKey: String,
    review: MediaAndReviewsReview?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        val shape = RoundedCornerShape(12.dp)
        val navigationCallback = LocalNavigationCallback.current
        UserAvatarImage(
            screenKey = screenKey,
            userId = review?.user?.id?.toString(),
            image = review?.user?.avatar?.large,
            contentScale = ContentScale.FillHeight,
            contentDescriptionTextRes = R.string.anime_media_details_reviews_user_avatar_content_description,
            modifier = Modifier
                .size(40.dp)
                .clip(shape)
                .border(width = Dp.Hairline, MaterialTheme.colorScheme.primary, shape)
                .clickable {
                    review?.user?.let {
                        navigationCallback.onUserClick(it, 1f)
                    }
                }
                .placeholder(
                    visible = review == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )

        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = review?.user?.name ?: "Username",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.placeholder(
                    visible = review == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
            )

            val timestamp = remember(review) {
                review?.let {
                    DateUtils.getRelativeTimeSpanString(
                        it.createdAt * 1000L,
                        Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond() * 1000,
                        0,
                        DateUtils.FORMAT_ABBREV_ALL,
                    )
                }
            }

            if (review == null || timestamp != null) {
                Text(
                    text = timestamp.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.placeholder(
                        visible = review == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                )
            }
        }

        ReviewRatingIconsSection(
            score = review?.score,
            rating = review?.rating,
            ratingAmount = review?.ratingAmount,
        )
    }

    Text(
        text = review?.summary.orEmpty(),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
    )
}

@Composable
fun ReviewCard(
    screenKey: String,
    viewer: AniListViewer?,
    review: MediaAndReviewsReview?,
    media: AnimeMediaCompactListRow.Entry?,
    onClick: (AnimeNavigator.NavigationCallback, CoilImageState) -> Unit,
    onClickListEdit: (MediaNavigationData) -> Unit,
    modifier: Modifier = Modifier,
    showMedia: Boolean = true,
) {
    val navigationCallback = LocalNavigationCallback.current
    val coverImageState = rememberCoilImageState(media?.media?.coverImage?.extraLarge)
    ElevatedCard(
        modifier = modifier
            .clickable(
                enabled = review?.id != null,
                onClick = { onClick(navigationCallback, coverImageState) },
            )
            .recomposeHighlighter()
    ) {
        ReviewSmallCardContent(screenKey, review)

        if (showMedia) {
            SharedTransitionKeyScope("anime_home_review", review?.id.toString()) {
                AnimeMediaCompactListRow(
                    viewer = viewer,
                    entry = media,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    onClickListEdit = onClickListEdit,
                    coverImageState = coverImageState,
                )
            }
        }
    }
}

@Composable
fun ReviewRatingIconsSection(
    score: Int?,
    rating: Int?,
    ratingAmount: Int?,
) {
    @Suppress("NAME_SHADOWING")
    val ratingAmount = ratingAmount ?: 0

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (score != null) {
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.labelLarge,
                )

                val iconTint = remember(score) {
                    when {
                        score > 80 -> Color.Green
                        score > 70 -> Color.Yellow
                        score > 50 -> Color(0xFFFF9000) // Orange
                        else -> Color.Red
                    }
                }
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = stringResource(
                        R.string.anime_media_rating_icon_content_description
                    ),
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (ratingAmount > 0) {
                Text(
                    text = ratingAmount.toString(),
                    style = MaterialTheme.typography.labelLarge,
                )

                Icon(
                    imageVector = when {
                        ratingAmount > 100 -> Icons.Filled.PeopleAlt
                        ratingAmount > 50 -> Icons.Outlined.PeopleAlt
                        ratingAmount > 10 -> Icons.Filled.Person
                        else -> Icons.Filled.PersonOutline
                    },
                    contentDescription = stringResource(
                        R.string.anime_media_details_reviews_rating_amount_content_description
                    ),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (rating != null) {
                Text(
                    text = rating.toString(),
                    style = MaterialTheme.typography.labelLarge,
                )

                val alpha = (rating / ratingAmount.toFloat()).coerceIn(0f, 1f)
                val tint = Color.Green.copy(alpha = alpha)
                    .compositeOver(LocalContentColor.current)
                Icon(
                    imageVector = Icons.Filled.ThumbUpAlt,
                    contentDescription = stringResource(
                        R.string.anime_media_details_reviews_rating_upvote_content_description
                    ),
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (rating != null && ratingAmount > 0) {
                val downvotes = ratingAmount - rating
                Text(
                    text = downvotes.toString(),
                    style = MaterialTheme.typography.labelLarge,
                )

                val alpha = (downvotes / ratingAmount.toFloat()).coerceIn(0f, 1f)
                val tint = Color.Red.copy(alpha = alpha)
                    .compositeOver(LocalContentColor.current)
                Icon(
                    imageVector = Icons.Filled.ThumbDownAlt,
                    contentDescription = stringResource(
                        R.string.anime_media_details_reviews_rating_downvote_content_description
                    ),
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun LazyListScope.reviewsSection(
    screenKey: String,
    entry: AnimeMediaDetailsReviewsViewModel.ReviewsEntry?,
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onClickViewAll: ((AnimeNavigator.NavigationCallback) -> Unit)? = null,
    onReviewClick: (AnimeNavigator.NavigationCallback, MediaDetails2Query.Data.Media.Reviews.Node) -> Unit,
) {
    if (entry != null && entry.reviews.isEmpty()) return
    listSection(
        titleRes = R.string.anime_media_details_reviews_label,
        values = entry?.reviews,
        valueToId = { it.id.toString() },
        aboveFold = ReviewComposables.REVIEWS_ABOVE_FOLD,
        hasMoreValues = entry?.hasMore ?: false,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        onClickViewAll = onClickViewAll,
        viewAllContentDescriptionTextRes = R.string.anime_media_details_view_all_content_description,
    ) { item, paddingBottom ->
        ReviewSmallCard(
            screenKey = screenKey,
            review = item,
            onClick = { onReviewClick(it, item) },
            modifier = Modifier
                .animateItem()
                .padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
        )
    }
}
