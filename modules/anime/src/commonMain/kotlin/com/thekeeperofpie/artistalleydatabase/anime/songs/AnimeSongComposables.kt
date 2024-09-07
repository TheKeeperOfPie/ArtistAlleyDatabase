package com.thekeeperofpie.artistalleydatabase.anime.songs

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_character_image_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_artist_image
import artistalleydatabase.modules.anime.generated.resources.anime_media_artist_no_image
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_song_artist_as_character
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_song_artist_with_character
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_song_ending
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_song_ending_episodes
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_song_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_song_open_link_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_song_opening
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_song_opening_episodes
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_song_pause_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_song_play_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_song_spoiler
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_song_spoiler_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_songs_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_voice_actor_image
import coil3.compose.AsyncImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSection
import com.thekeeperofpie.artistalleydatabase.media.MediaPlayerView
import com.thekeeperofpie.artistalleydatabase.media.rememberMediaPlayerViewState
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import org.jetbrains.compose.resources.stringResource

object AnimeSongComposables {

    const val SONGS_ABOVE_FOLD = 3

    context (LazyListScope)
    fun songsSection(
        viewModel: AnimeSongsViewModel,
        songsExpanded: () -> Boolean,
        onSongsExpandedChange: (Boolean) -> Unit,
    ) {
        val animeSongs = viewModel.animeSongs ?: return
        listSection(
            titleRes = Res.string.anime_media_details_songs_label,
            values = animeSongs.entries,
            valueToId = { it.id },
            aboveFold = SONGS_ABOVE_FOLD,
            expanded = songsExpanded,
            onExpandedChange = onSongsExpandedChange,
        ) { item, paddingBottom ->
            SharedTransitionKeyScope("song_row", item.id) {
                AnimeThemeRow(
                    viewModel = viewModel,
                    entry = item,
                    modifier = Modifier
                        .animateItem()
                        .padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
                )
            }
        }
    }

    @Composable
    private fun AnimeThemeRow(
        viewModel: AnimeSongsViewModel,
        entry: AnimeSongEntry,
        modifier: Modifier = Modifier,
    ) {
        val state = viewModel.getAnimeSongState(entry.id)
        val mediaPlayer = viewModel.mediaPlayer
        val active by remember {
            derivedStateOf { mediaPlayer.activeId == entry.id }
        }
        val playing = active && mediaPlayer.playing

        ElevatedCard(
            modifier = Modifier
                .animateContentSize()
                .then(modifier),
        ) {
            var hidden by remember { mutableStateOf(entry.spoiler && !active) }
            if (hidden) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { hidden = false }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = stringResource(
                            Res.string.anime_media_details_song_spoiler_content_description
                        ),
                    )

                    Text(
                        text = stringResource(Res.string.anime_media_details_song_spoiler),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f)
                    )
                }
            } else {
                // TODO: This doesn't line up perfectly (too much space between label and title),
                //  consider migrating to ConstraintLayout
                Column(
                    modifier = Modifier.clickable {
                        viewModel.onAnimeSongExpandedToggle(entry.id, !state.expanded())
                    },
                ) {
                    Row {
                        if (entry.spoiler) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = stringResource(
                                    Res.string.anime_media_details_song_spoiler_content_description
                                ),
                                modifier = Modifier
                                    .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        }

                        val labelText = when (entry.type) {
                            AnimeSongEntry.Type.OP -> if (entry.episodes.isNullOrBlank()) {
                                stringResource(Res.string.anime_media_details_song_opening)
                            } else {
                                stringResource(
                                    Res.string.anime_media_details_song_opening_episodes,
                                    entry.episodes,
                                )
                            }
                            AnimeSongEntry.Type.ED -> if (entry.episodes.isNullOrBlank()) {
                                stringResource(Res.string.anime_media_details_song_ending)
                            } else {
                                stringResource(
                                    Res.string.anime_media_details_song_ending_episodes,
                                    entry.episodes,
                                )
                            }
                            null -> null
                        }

                        Text(
                            text = labelText.orEmpty(),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.surfaceTint,
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(Alignment.CenterVertically)
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        if (entry.videoUrl != null || entry.audioUrl != null) {
                            if (!state.expanded()) {
                                IconButton(
                                    onClick = { viewModel.onAnimeSongPlayAudioClick(entry.id) },
                                ) {
                                    if (playing) {
                                        Icon(
                                            imageVector = Icons.Filled.PauseCircleOutline,
                                            contentDescription = stringResource(
                                                Res.string.anime_media_details_song_pause_content_description
                                            ),
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.PlayCircleOutline,
                                            contentDescription = stringResource(
                                                Res.string.anime_media_details_song_play_content_description
                                            ),
                                        )
                                    }
                                }
                            }

                            if (entry.videoUrl != null) {
                                TrailingDropdownIconButton(
                                    expanded = state.expanded(),
                                    contentDescription = stringResource(
                                        Res.string.anime_media_details_song_expand_content_description
                                    ),
                                    onClick = {
                                        viewModel.onAnimeSongExpandedToggle(
                                            entry.id,
                                            !state.expanded(),
                                        )
                                    },
                                )
                            }
                        }
                    }

                    // TODO: Expanding above another entry causes a layout issue
                    if (state.expanded()) {
                        Box {
                            val mediaPlayerViewState = rememberMediaPlayerViewState()
                            MediaPlayerView(
                                mediaPlayer = mediaPlayer,
                                state = mediaPlayerViewState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .heightIn(min = 180.dp)
                            )

                            val uriHandler = LocalUriHandler.current
                            val alpha by animateFloatAsState(
                                targetValue = if (mediaPlayerViewState.controlsVisible) 1f else 0f,
                                label = "Song open link button alpha",
                            )
                            IconButton(
                                onClick = { uriHandler.openUri(entry.link!!) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .alpha(alpha)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.OpenInBrowser,
                                    contentDescription = stringResource(
                                        Res.string.anime_media_details_song_open_link_content_description
                                    ),
                                )
                            }
                        }
                    } else if (active) {
                        val progress = mediaPlayer.progress
                        Slider(
                            value = progress,
                            onValueChange = { viewModel.onAnimeSongProgressUpdate(entry.id, it) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }

                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = if (state.expanded()) 10.dp else 0.dp,
                                bottom = 10.dp
                            )
                    )
                }
            }

            if (!hidden) {
                val uriHandler = LocalUriHandler.current
                val artists = entry.artists
                if (artists.isNotEmpty()) {
                    artists.forEachIndexed { index, artist ->
                        val isLast = index == artists.lastIndex
                        HorizontalDivider()
                        Row(
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .clickable { uriHandler.openUri(artist.link) }
                        ) {
                            val artistImage = artist.image
                            val characterImage = artist.character?.image

                            @Composable
                            fun ArtistImage(modifier: Modifier) {
                                val sharedTransitionKey = artist.aniListId
                                    ?.let { SharedTransitionKey.makeKeyForId(it) }
                                AsyncImage(
                                    model = artistImage,
                                    contentScale = ContentScale.FillHeight,
                                    fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                                    contentDescription = stringResource(
                                        if (artist.asCharacter) {
                                            Res.string.anime_media_voice_actor_image
                                        } else {
                                            Res.string.anime_media_artist_image
                                        }
                                    ),
                                    modifier = modifier
                                        .sizeIn(minWidth = 44.dp, minHeight = 64.dp)
                                        .conditionally(artist.aniListId != null) {
                                            sharedElement(sharedTransitionKey, "staff_image")
                                        }
                                        .fillMaxHeight()
                                )
                            }

                            @Composable
                            fun CharacterImage(modifier: Modifier) {
                                val characterId = artist.character?.aniListId
                                val sharedTransitionKey =
                                    characterId?.let { SharedTransitionKey.makeKeyForId(it) }
                                AsyncImage(
                                    model = characterImage!!,
                                    contentScale = ContentScale.FillHeight,
                                    fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                                    contentDescription = stringResource(
                                        Res.string.anime_character_image_content_description
                                    ),
                                    modifier = modifier
                                        .sizeIn(minWidth = 44.dp, minHeight = 64.dp)
                                        .conditionally(characterId != null) {
                                            sharedElement(sharedTransitionKey, "character_image")
                                                .clickable {
                                                    // TODO: Use navigation callback
                                                    uriHandler.openUri(artist.character.link)
                                                }
                                        }
                                        .fillMaxHeight()
                                )
                            }

                            val firstImage: (@Composable (modifier: Modifier) -> Unit)?
                            val secondImage: (@Composable (modifier: Modifier) -> Unit)?

                            val asCharacter = artist.asCharacter
                            if (asCharacter) {
                                if (characterImage == null) {
                                    if (artistImage == null) {
                                        firstImage = null
                                        secondImage = null
                                    } else {
                                        firstImage = { ArtistImage(it) }
                                        secondImage = null
                                    }
                                } else {
                                    firstImage = { CharacterImage(it) }
                                    secondImage = { ArtistImage(it) }
                                }
                            } else {
                                firstImage = { ArtistImage(it) }
                                secondImage = characterImage?.let { { CharacterImage(it) } }
                            }

                            if (firstImage == null) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(width = 44.dp, height = 64.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = stringResource(
                                            Res.string.anime_media_artist_no_image
                                        ),
                                    )
                                }
                            } else {
                                firstImage(Modifier.conditionally(isLast) {
                                    clip(RoundedCornerShape(bottomStart = 12.dp))
                                })
                            }

                            val artistText = if (artist.character == null) {
                                artist.name
                            } else if (artist.asCharacter) {
                                stringResource(
                                    Res.string.anime_media_details_song_artist_as_character,
                                    artist.character.name(),
                                    artist.name,
                                )
                            } else {
                                stringResource(
                                    Res.string.anime_media_details_song_artist_with_character,
                                    artist.name,
                                    artist.character.name(),
                                )
                            }

                            Text(
                                text = artistText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterVertically)
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            )

                            if (secondImage != null) {
                                secondImage(Modifier.conditionally(isLast) {
                                    clip(RoundedCornerShape(bottomEnd = 12.dp))
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}
