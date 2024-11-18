@file:OptIn(ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.recommendations

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridScope
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.recommendations.generated.resources.Res
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_media_details_recommendations_label
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_media_recommendation_rate_down_empty_content_description
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_media_recommendation_rate_down_filled_content_description
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_media_recommendation_rate_up_empty_content_description
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_media_recommendation_rate_up_filled_content_description
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_user_avatar_content_description
import artistalleydatabase.modules.anime.ui.generated.resources.anime_generic_view_all_content_description
import com.anilist.data.fragment.UserNavigationData
import com.anilist.data.type.RecommendationRating
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.AnimeMediaDetailsRecommendationsViewModel.Recommendation
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavHostController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.recomposeHighlighter
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.anime.ui.generated.resources.Res as UiRes

object RecommendationComposables {
    const val RECOMMENDATIONS_ABOVE_FOLD = 3
}

@Composable
fun <MediaEntry> RecommendationCard(
    viewer: AniListViewer?,
    user: UserNavigationData?,
    media: MediaEntry?,
    recommendation: RecommendationData?,
    onUserRecommendationRating: (
        recommendation: RecommendationData,
        newRating: RecommendationRating,
    ) -> Unit = { _, _ -> },
    mediaRows: @Composable () -> Unit,
    userRoute: (
        userId: String,
        userName: String,
        coverImageState: ImageState?,
        sharedTransitionKey: SharedTransitionKey?,
    ) -> NavDestination,
) {
    ElevatedCard(modifier = Modifier.recomposeHighlighter()) {
        if (user != null || media == null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                val shape = RoundedCornerShape(12.dp)
                val navHostController = LocalNavHostController.current
                val imageState = rememberCoilImageState(user?.avatar?.large)
                val sharedTransitionKey = user?.id?.toString()
                    ?.let { SharedTransitionKey.makeKeyForId(it) }
                UserAvatarImage(
                    imageState = imageState,
                    image = imageState.request().build(),
                    modifier = Modifier
                        .size(40.dp)
                        .sharedElement(sharedTransitionKey, "user_image")
                        .clip(shape)
                        .border(width = Dp.Hairline, MaterialTheme.colorScheme.primary, shape)
                        .clickable {
                            user?.let {
                                navHostController.navigate(
                                    userRoute(
                                        user.id.toString(),
                                        user.name,
                                        imageState.toImageState(),
                                        sharedTransitionKey,
                                    )
                                )
                            }
                        }
                        .placeholder(
                            visible = media == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        ),
                    contentScale = ContentScale.FillHeight,
                    contentDescriptionTextRes = Res.string.anime_recommendation_user_avatar_content_description
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

        mediaRows()

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
                            Res.string.anime_media_recommendation_rate_down_filled_content_description
                        } else {
                            Res.string.anime_media_recommendation_rate_down_empty_content_description
                        }
                    ),
                )
            }

            Text(
                text = recommendation?.rating?.toString() ?: "0",
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
                            Res.string.anime_media_recommendation_rate_up_filled_content_description
                        } else {
                            Res.string.anime_media_recommendation_rate_up_empty_content_description
                        }
                    ),
                )
            }
        }
    }
}

fun <MediaEntry> LazyGridScope.recommendationsSection(
    entry: AnimeMediaDetailsRecommendationsViewModel.RecommendationsEntry<MediaEntry>?,
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onClickViewAll: (() -> Unit)? = null,
    mediaId: (Recommendation<MediaEntry>) -> String,
    mediaRow: @Composable (Recommendation<MediaEntry>, Modifier) -> Unit,
) {
    listSection(
        titleRes = Res.string.anime_media_details_recommendations_label,
        values = entry?.recommendations,
        valueToId = { "anime_media_${mediaId(it)}" },
        aboveFold = RecommendationComposables.RECOMMENDATIONS_ABOVE_FOLD,
        hasMoreValues = entry?.hasMore ?: false,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        onClickViewAll = onClickViewAll,
        viewAllContentDescriptionTextRes = UiRes.string.anime_generic_view_all_content_description,
    ) { item, paddingBottom ->
        mediaRow(
            item,
            Modifier
                .animateItem()
                .padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
        )
    }
}
