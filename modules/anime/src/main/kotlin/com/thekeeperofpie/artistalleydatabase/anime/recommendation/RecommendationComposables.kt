package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anilist.fragment.UserNavigationData
import com.anilist.type.RecommendationRating
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage

@Composable
fun RecommendationCard(
    screenKey: String,
    viewer: AniListViewer?,
    user: UserNavigationData?,
    media: AnimeMediaCompactListRow.Entry?,
    mediaRecommendation: AnimeMediaCompactListRow.Entry?,
    onClickListEdit: (AnimeMediaCompactListRow.Entry) -> Unit,
    recommendation: RecommendationData?,
    onUserRecommendationRating: (
        recommendation: RecommendationData,
        newRating: RecommendationRating,
    ) -> Unit = { _, _ -> },
) {
    ElevatedCard {
        if (user != null || media == null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                val shape = RoundedCornerShape(12.dp)
                val navigationCallback = LocalNavigationCallback.current
                UserAvatarImage(
                    screenKey = screenKey,
                    userId = user?.id?.toString(),
                    image = user?.avatar?.large,
                    contentScale = ContentScale.FillHeight,
                    contentDescriptionTextRes = R.string.anime_recommendation_user_avatar_content_description,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(shape)
                        .border(width = Dp.Hairline, MaterialTheme.colorScheme.primary, shape)
                        .clickable {
                            user?.let {
                                navigationCallback.onUserClick(it, 1f)
                            }
                        }
                        .placeholder(
                            visible = media == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )

                Text(
                    text = user?.name ?: "Username",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .placeholder(
                            visible = media == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }
        }

        AnimeMediaCompactListRow(
            screenKey = screenKey,
            viewer = viewer,
            entry = media,
            onClickListEdit = onClickListEdit,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
        )

        AnimeMediaCompactListRow(
            screenKey = screenKey,
            viewer = viewer,
            entry = mediaRecommendation,
            onClickListEdit = onClickListEdit,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val userRating = recommendation?.userRating
            IconButton(
                enabled = viewer != null,
                onClick = {
                    if (recommendation != null) {
                        val newRating = if (userRating == RecommendationRating.RATE_DOWN) {
                            RecommendationRating.NO_RATING
                        } else {
                            RecommendationRating.RATE_DOWN
                        }
                        onUserRecommendationRating(recommendation, newRating)
                    }
                },
            ) {
                Icon(
                    imageVector = if (userRating == RecommendationRating.RATE_DOWN) {
                        Icons.Filled.ThumbDown
                    } else {
                        Icons.Outlined.ThumbDown
                    },
                    contentDescription = stringResource(
                        if (userRating == RecommendationRating.RATE_DOWN) {
                            R.string.anime_media_recommendation_rate_down_filled_content_description
                        } else {
                            R.string.anime_media_recommendation_rate_down_empty_content_description
                        }
                    ),
                )
            }

            Text(
                text = recommendation?.rating?.toString() ?: "00",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 16.dp)
                    .placeholder(
                        visible = recommendation == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )

            IconButton(
                enabled = viewer != null,
                onClick = {
                    if (recommendation != null) {
                        val newRating = if (userRating == RecommendationRating.RATE_UP) {
                            RecommendationRating.NO_RATING
                        } else {
                            RecommendationRating.RATE_UP
                        }
                        onUserRecommendationRating(recommendation, newRating)
                    }
                },
            ) {
                Icon(
                    imageVector = if (userRating == RecommendationRating.RATE_UP) {
                        Icons.Filled.ThumbUp
                    } else {
                        Icons.Outlined.ThumbUp
                    },
                    contentDescription = stringResource(
                        if (userRating == RecommendationRating.RATE_UP) {
                            R.string.anime_media_recommendation_rate_up_filled_content_description
                        } else {
                            R.string.anime_media_recommendation_rate_up_empty_content_description
                        }
                    ),
                )
            }
        }
    }
}
