package com.thekeeperofpie.artistalleydatabase.anime.review.details

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
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_reviews_user_avatar_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_review_details_downvote_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_review_details_score
import artistalleydatabase.modules.anime.generated.resources.anime_review_details_upvote_content_description
import com.anilist.data.ReviewDetailsQuery
import com.anilist.data.type.ReviewRating
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.data.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewRatingIconsSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsLoadingOrError
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.anime.user.UserHeaderParams
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.ImageHtmlText
import com.thekeeperofpie.artistalleydatabase.utils_compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import kotlinx.datetime.Instant
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object ReviewDetailsScreen {

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
                    val coverImageState = rememberCoilImageState(media?.coverImage?.extraLarge)
                    val title = entry?.review?.media?.title?.primaryTitle()
                    val mediaSharedTransitionKey = entry?.review?.media?.id?.toString()
                        ?.let { SharedTransitionKey.makeKeyForId(it) }
                    MediaHeader(
                        viewer = viewModel.viewer.collectAsState().value,
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
                        coverImageState = coverImageState,
                        enableCoverImageSharedElement = false,
                        onCoverImageClick = {
                            entry?.review?.media?.let { media ->
                                navigationCallback.navigate(
                                    AnimeDestination.MediaDetails(
                                        mediaId = media.id.toString(),
                                        title = title,
                                        coverImage = coverImageState.toImageState(),
                                        sharedTransitionKey = mediaSharedTransitionKey,
                                        headerParams = MediaHeaderParams(
                                            title = title,
                                            coverImage = coverImageState.toImageState(),
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
                        val navigationCallback = LocalNavigationCallback.current
                        val shape = RoundedCornerShape(12.dp)
                        val imageState = rememberCoilImageState(review.user?.avatar?.large)
                        val sharedTransitionKey = review.user?.id?.toString()
                            ?.let { SharedTransitionKey.makeKeyForId(it) }
                        UserAvatarImage(
                            imageState = imageState,
                            image = imageState.request().build(),
                            modifier = Modifier
                                .size(64.dp)
                                .sharedElement(sharedTransitionKey, "user_image")
                                .clip(shape)
                                .border(width = Dp.Hairline, MaterialTheme.colorScheme.primary, shape)
                                .clickable {
                                    review.user?.let {
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
                                },
                            contentScale = ContentScale.FillHeight,
                            contentDescriptionTextRes = Res.string.anime_media_details_reviews_user_avatar_content_description,
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
                        HumanReadable.timeAgo(Instant.fromEpochSeconds(review.createdAt.toLong()))
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
                                        Res.string.anime_review_details_score,
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
                                        Res.string.anime_review_details_upvote_content_description
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
