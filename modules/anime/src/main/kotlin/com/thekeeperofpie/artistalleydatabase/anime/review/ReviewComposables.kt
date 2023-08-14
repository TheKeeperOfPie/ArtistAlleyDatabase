package com.thekeeperofpie.artistalleydatabase.anime.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anilist.fragment.MediaAndReviewsReview
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 16.dp)
                .height(IntrinsicSize.Min)
                .heightIn(min = 72.dp)
        ) {
            var imageWidthToHeightRatio by remember { MutableSingle(1f) }
            UserAvatarImage(
                screenKey = screenKey,
                userId = review?.user?.id?.toString(),
                image = review?.user?.avatar?.large,
                contentScale = ContentScale.FillHeight,
                contentDescriptionTextRes = R.string.anime_media_details_reviews_user_avatar_content_description,
                onSuccess = { imageWidthToHeightRatio = it.widthToHeightRatio() },
                modifier = Modifier
                    .heightIn(min = 64.dp)
                    .padding(vertical = 10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        review?.user?.let {
                            navigationCallback.onUserClick(it, imageWidthToHeightRatio)
                        }
                    },
            )

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = review?.user?.name.orEmpty(),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .align(Alignment.CenterStart)
                )
            }

            ReviewRatingIconsSection(
                score = review?.score,
                rating = review?.rating,
                ratingAmount = review?.ratingAmount,
                modifier = Modifier.align(Alignment.Top)
            )
        }

        Text(
            text = review?.summary.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun ReviewRatingIconsSection(
    score: Int?,
    rating: Int?,
    ratingAmount: Int?,
    modifier: Modifier = Modifier,
    showDownvotes: Boolean = false,
) {
    @Suppress("NAME_SHADOWING")
    val ratingAmount = ratingAmount ?: 0

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .height(24.dp),
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

        if (rating != null) {
            Text(
                text = rating.toString(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 12.dp),
            )

            // If showing downvotes, just show a green upvote counter
            if (showDownvotes) {
                Icon(
                    imageVector = Icons.Filled.ThumbUpAlt,
                    contentDescription = stringResource(
                        R.string.anime_media_details_reviews_rating_upvote_content_description
                    ),
                    tint = Color.Green,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                val ratio = rating / ratingAmount.toFloat()
                val iconTint = when {
                    ratio > 0.6f -> Color.Green
                    ratio > 0.4f -> Color.Yellow
                    ratio > 0.2f -> Color(0xFFFF9000) // Orange
                    else -> Color.Red
                }
                Icon(
                    imageVector = if (ratio > 0.4f) {
                        Icons.Filled.ThumbUpAlt
                    } else {
                        Icons.Filled.ThumbDownAlt
                    },
                    contentDescription = stringResource(
                        R.string.anime_media_details_reviews_rating_upvote_content_description
                    ),
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (rating != null && showDownvotes && ratingAmount > 0) {
            val downvotes = ratingAmount - rating
            Text(
                text = downvotes.toString(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 12.dp),
            )

            Icon(
                imageVector = Icons.Filled.ThumbDownAlt,
                contentDescription = stringResource(
                    R.string.anime_media_details_reviews_rating_downvote_content_description
                ),
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
        }

        if (ratingAmount > 0) {
            Text(
                text = ratingAmount.toString(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 12.dp),
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
}
