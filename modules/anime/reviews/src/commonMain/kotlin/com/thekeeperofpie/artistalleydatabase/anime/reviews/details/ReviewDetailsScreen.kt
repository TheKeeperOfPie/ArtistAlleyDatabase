package com.thekeeperofpie.artistalleydatabase.anime.reviews.details

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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.reviews.generated.resources.Res
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_media_details_reviews_user_avatar_content_description
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_review_details_downvote_content_description
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_review_details_score
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_review_details_upvote_content_description
import com.anilist.data.ReviewDetailsQuery
import com.anilist.data.type.ReviewRating
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataUtils
import com.thekeeperofpie.artistalleydatabase.anime.reviews.ReviewRatingIconsSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.ImageHtmlText
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import kotlinx.datetime.Instant
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object ReviewDetailsScreen {

    @Composable
    operator fun invoke(
        viewer: () -> AniListViewer?,
        entry: () -> LoadingResult<Entry>,
        mediaHeader: @Composable (progress: Float) -> Unit,
        userRating: () -> LoadingResult<ReviewRating>,
        onUserRating: (Boolean?) -> Unit,
        userRoute: UserRoute,
    ) {
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
                    mediaHeader(it)
                }
            },
            modifier = Modifier.Companion.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Column(
                modifier = Modifier.Companion
                    .padding(it)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                val entry = entry()
                val review = entry.result?.review
                if (review == null) {
                    if (entry.loading) {
                        Box(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.Companion
                                    .align(Alignment.Companion.Center)
                                    .padding(32.dp)
                            )
                        }
                    } else {
                        val error = entry.error
                        VerticalList.ErrorContent(
                            errorText = error?.message().orEmpty(),
                            exception = error?.throwable,
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.Companion.CenterVertically,
                        modifier = Modifier.Companion
                            .height(IntrinsicSize.Min)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        val navigationController = LocalNavigationController.current
                        val shape = RoundedCornerShape(12.dp)
                        val imageState = rememberCoilImageState(review.user?.avatar?.large)
                        val sharedTransitionKey = review.user?.id?.toString()
                            ?.let { SharedTransitionKey.Companion.makeKeyForId(it) }
                        UserAvatarImage(
                            imageState = imageState,
                            image = imageState.request().build(),
                            modifier = Modifier.Companion
                                .size(64.dp)
                                .sharedElement(sharedTransitionKey, "user_image")
                                .clip(shape)
                                .border(
                                    width = Dp.Companion.Hairline,
                                    MaterialTheme.colorScheme.primary,
                                    shape
                                )
                                .clickable {
                                    review.user?.let {
                                        navigationController.navigate(
                                            userRoute(
                                                it.id.toString(),
                                                sharedTransitionKey,
                                                it.name,
                                                imageState.toImageState(),
                                            )
                                        )
                                    }
                                },
                            contentScale = ContentScale.Companion.FillHeight,
                            contentDescriptionTextRes = Res.string
                                .anime_media_details_reviews_user_avatar_content_description,
                        )

                        AutoSizeText(
                            text = review.user?.name.orEmpty(),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.Companion
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
                        HumanReadable.timeAgo(Instant.Companion.fromEpochSeconds(review.createdAt.toLong()))
                    }

                    Text(
                        text = timestamp.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.Companion.padding(horizontal = 16.dp)
                    )

                    ImageHtmlText(
                        text = review.body.orEmpty(),
                        color = MaterialTheme.typography.bodyMedium.color
                            .takeOrElse { LocalContentColor.current },
                        modifier = Modifier.Companion.padding(16.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.Companion.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.Companion.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 32.dp
                        ),
                    ) {
                        val ratingColor = review.score?.let { MediaDataUtils.ratingColor(it) }

                        Box(
                            contentAlignment = Alignment.Companion.Center,
                            modifier = Modifier.Companion.weight(1f)
                        ) {
                            ElevatedCard(
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = ratingColor
                                        ?: MaterialTheme.colorScheme.surface,
                                ),
                            ) {
                                Text(
                                    text = stringResource(
                                        Res.string.anime_review_details_score,
                                        review.score?.toString() ?: "-"
                                    ),
                                    color = ratingColor?.let(ComposeColorUtils::bestTextColor)
                                        ?: Color.Companion.Unspecified,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.Companion.padding(
                                        horizontal = 16.dp,
                                        vertical = 10.dp
                                    )
                                )
                            }
                        }

                        if (viewer() != null) {
                            val userRating = userRating()
                            val isUpvoted = userRating.result == ReviewRating.UP_VOTE
                            IconButton(onClick = {
                                onUserRating(true.takeUnless { isUpvoted })
                            }) {
                                Icon(
                                    imageVector = if (isUpvoted) {
                                        Icons.Filled.ThumbUp
                                    } else {
                                        Icons.Outlined.ThumbUp
                                    },
                                    contentDescription = stringResource(
                                        Res.string.anime_review_details_upvote_content_description
                                    )
                                )
                            }

                            val isDownvoted = userRating.result == ReviewRating.DOWN_VOTE
                            IconButton(onClick = {
                                onUserRating(false.takeUnless { isDownvoted })
                            }) {
                                Icon(
                                    imageVector = if (isDownvoted) {
                                        Icons.Filled.ThumbDown
                                    } else {
                                        Icons.Outlined.ThumbDown
                                    },
                                    tint = if (isDownvoted) Color.Companion.Red else LocalContentColor.current,
                                    contentDescription = stringResource(
                                        Res.string.anime_review_details_downvote_content_description
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
