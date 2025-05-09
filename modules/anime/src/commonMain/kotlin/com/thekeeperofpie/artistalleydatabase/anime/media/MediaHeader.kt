@file:OptIn(ExperimentalCoilApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_more_actions_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_open_external
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_open_external_icon_content_description
import coil3.annotation.ExperimentalCoilApi
import com.anilist.data.type.MediaFormat
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListDataUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaRatingIconsSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedBounds
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import org.jetbrains.compose.resources.stringResource

@Composable
fun MediaHeader(
    viewer: AniListViewer?,
    upIconOption: UpIconOption,
    mediaId: String?,
    mediaType: MediaType?,
    titles: List<String>?,
    episodes: Int?,
    format: MediaFormat?,
    averageScore: Int?,
    popularity: Int?,
    progress: Float,
    headerValues: MediaHeaderValues,
    onFavoriteChanged: (Boolean) -> Unit,
    sharedTransitionKey: SharedTransitionKey? = null,
    coverImageState: CoilImageState? = rememberCoilImageState(headerValues.coverImage),
    enableCoverImageSharedElement: Boolean = true,
    onCoverImageClick: (() -> Unit)? = null,
    menuContent: @Composable() (() -> Unit)? = null,
) {
    val defaultTitle = headerValues.title().orEmpty()
    var preferredTitle by remember(defaultTitle, titles) {
        mutableIntStateOf(
            titles?.indexOf(defaultTitle)?.coerceAtLeast(0) ?: 0
        )
    }
    CoverAndBannerHeader(
        upIconOption = upIconOption,
        headerValues = headerValues,
        coverImageState = coverImageState,
        sharedTransitionKey = sharedTransitionKey.takeIf { enableCoverImageSharedElement },
        coverImageSharedTransitionIdentifier = "media_image",
        bannerImageSharedTransitionIdentifier = "media_banner_image",
        progress = progress,
        onClickEnabled = (titles?.size ?: 0) > 1,
        onClick = {
            preferredTitle = (preferredTitle + 1) % (titles?.size ?: 1)
        },
        menuContent = if (viewer == null) null else {
            {
                FavoriteIconButton(
                    favorite = headerValues.favorite,
                    onFavoriteChanged = onFavoriteChanged,
                )
            }
        },
        onCoverImageClick = onCoverImageClick,
        modifier = Modifier.sharedBounds(sharedTransitionKey, "media_header")
    ) {
        Column {
            Row(modifier = Modifier.weight(1f)) {
                AutoResizeHeightText(
                    text = titles?.getOrNull(preferredTitle) ?: headerValues.title() ?: "",
                    style = MaterialTheme.typography.headlineLarge
                        .copy(lineBreak = LineBreak.Simple),
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                )
                val ratingIconsAlpha = if (progress > 0.6f) 1f - ((1f - progress) / 0.2f) else 0f
                AnimatedVisibility(
                    visible = ratingIconsAlpha != 0f,
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally()
                ) {
                    MediaRatingIconsSection(
                        rating = averageScore,
                        popularity = popularity,
                        modifier = Modifier
                            .alpha(ratingIconsAlpha)
                            .padding(start = 8.dp, end = 8.dp, top = 12.dp)
                            .align(Alignment.Top)
                    )
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 10.dp)
                ) {
                    val subtitleText = headerValues.subtitleText()
                    AnimatedVisibility(
                        subtitleText != null,
                        label = "Media details subtitle text"
                    ) {
                        if (subtitleText != null) {
                            Text(
                                text = subtitleText,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .fillMaxWidth()
                                    .wrapContentHeight(Alignment.Bottom)
                            )
                        }
                    }

                    val nextAiringEpisode = headerValues.nextAiringEpisode
                    AnimatedVisibility(
                        nextAiringEpisode != null,
                        label = "Media details nextAiringEpisode text"
                    ) {
                        if (nextAiringEpisode != null) {
                            val nextAiringAtText = MediaUtils.nextAiringSectionText(
                                nextAiringEpisode = nextAiringEpisode,
                                episodes = episodes,
                                format = format,
                            )

                            Text(
                                text = nextAiringAtText,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.surfaceTint,
                                modifier = Modifier
                                    .wrapContentHeight(Alignment.Bottom)
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (menuContent != null) {
                    menuContent()
                } else {
                    Box {
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(
                                    Res.string.anime_media_details_more_actions_content_description,
                                ),
                            )
                        }

                        val uriHandler = LocalUriHandler.current
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.anime_media_details_open_external)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.OpenInBrowser,
                                        contentDescription = stringResource(
                                            Res.string.anime_media_details_open_external_icon_content_description
                                        )
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    uriHandler.openUri(
                                        AniListDataUtils.mediaUrl(
                                            // TODO: Better infer media type
                                            mediaType ?: MediaType.ANIME,
                                            mediaId.toString()
                                        ) + "?${UriUtils.FORCE_EXTERNAL_URI_PARAM}=true"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
