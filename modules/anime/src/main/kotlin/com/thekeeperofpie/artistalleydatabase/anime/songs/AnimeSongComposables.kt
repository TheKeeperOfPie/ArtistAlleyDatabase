package com.thekeeperofpie.artistalleydatabase.anime.songs

import android.view.View
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSection
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.conditionally

@OptIn(ExperimentalFoundationApi::class)
object AnimeSongComposables {

    const val SONGS_ABOVE_FOLD = 3

    context (LazyListScope)
    fun songsSection(
        viewModel: AnimeSongsViewModel,
        screenKey: String,
        songsExpanded: () -> Boolean,
        onSongsExpandedChange: (Boolean) -> Unit,
    ) {
        val animeSongs = viewModel.animeSongs ?: return
        listSection(
            titleRes = R.string.anime_media_details_songs_label,
            values = animeSongs.entries,
            valueToId = { it.id },
            aboveFold = SONGS_ABOVE_FOLD,
            expanded = songsExpanded,
            onExpandedChange = onSongsExpandedChange,
        ) { item, paddingBottom ->
            AnimeThemeRow(
                screenKey,
                viewModel = viewModel,
                entry = item,
                modifier = Modifier
                    .animateItem()
                    .padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
            )
        }
    }

    @Composable
    private fun AnimeThemeRow(
        screenKey: String,
        viewModel: AnimeSongsViewModel,
        entry: AnimeSongEntry,
        modifier: Modifier = Modifier,
    ) {
        val state = viewModel.getAnimeSongState(entry.id)
        val mediaPlayer = viewModel.mediaPlayer
        val playingState by mediaPlayer.playingState.collectAsState()
        val active by remember {
            derivedStateOf { playingState.first == entry.id }
        }
        val playing = active && playingState.second

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
                            R.string.anime_media_details_song_spoiler_content_description
                        ),
                    )

                    Text(
                        text = stringResource(R.string.anime_media_details_song_spoiler),
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
                                    R.string.anime_media_details_song_spoiler_content_description
                                ),
                                modifier = Modifier
                                    .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        }

                        val labelText = when (entry.type) {
                            AnimeSongEntry.Type.OP -> if (entry.episodes.isNullOrBlank()) {
                                stringResource(R.string.anime_media_details_song_opening)
                            } else {
                                stringResource(
                                    R.string.anime_media_details_song_opening_episodes,
                                    entry.episodes,
                                )
                            }
                            AnimeSongEntry.Type.ED -> if (entry.episodes.isNullOrBlank()) {
                                stringResource(R.string.anime_media_details_song_ending)
                            } else {
                                stringResource(
                                    R.string.anime_media_details_song_ending_episodes,
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
                                                R.string.anime_media_details_song_pause_content_description
                                            ),
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.PlayCircleOutline,
                                            contentDescription = stringResource(
                                                R.string.anime_media_details_song_play_content_description
                                            ),
                                        )
                                    }
                                }
                            }

                            if (entry.videoUrl != null) {
                                TrailingDropdownIconButton(
                                    expanded = state.expanded(),
                                    contentDescription = stringResource(
                                        R.string.anime_media_details_song_expand_content_description
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

                    if (state.expanded()) {
                        Box {
                            var linkButtonVisible by remember { mutableStateOf(true) }
                            AndroidView(
                                factory = {
                                    @Suppress("UnsafeOptInUsageError")
                                    PlayerView(it).apply {
                                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                                        setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                                        setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE)
                                        setControllerVisibilityListener(
                                            PlayerView.ControllerVisibilityListener {
                                                linkButtonVisible = it == View.VISIBLE
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .heightIn(min = 180.dp),
                                update = { it.player = mediaPlayer.player },
                                onReset = { it.player = null },
                                onRelease = { it.player = null },
                            )

                            val uriHandler = LocalUriHandler.current
                            val alpha by animateFloatAsState(
                                targetValue = if (linkButtonVisible) 1f else 0f,
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
                                        R.string.anime_media_details_song_open_link_content_description
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
                        Divider()
                        Row(
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .clickable { uriHandler.openUri(artist.link) }
                        ) {
                            val artistImage = artist.image
                            val characterImage = artist.character?.image

                            @Composable
                            fun ArtistImage(modifier: Modifier) {
                                val image = @Composable {
                                    AsyncImage(
                                        model = artistImage,
                                        contentScale = ContentScale.FillHeight,
                                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                                        contentDescription = stringResource(
                                            if (artist.asCharacter) {
                                                R.string.anime_media_voice_actor_image
                                            } else {
                                                R.string.anime_media_artist_image
                                            }
                                        ),
                                        modifier = modifier
                                            .sizeIn(minWidth = 44.dp, minHeight = 64.dp)
                                            .fillMaxHeight()
                                    )
                                }
                                if (artist.aniListId != null) {
                                    SharedElement(
                                        key = "anime_staff_${artist.aniListId}_image",
                                        screenKey = screenKey
                                    ) {
                                        image()
                                    }
                                } else {
                                    image()
                                }
                            }

                            @Composable
                            fun CharacterImage(modifier: Modifier) {
                                val image = @Composable { modifier: Modifier ->
                                    AsyncImage(
                                        model = characterImage!!,
                                        contentScale = ContentScale.FillHeight,
                                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                                        contentDescription = stringResource(
                                            R.string.anime_character_image_content_description
                                        ),
                                        modifier = modifier
                                            .sizeIn(minWidth = 44.dp, minHeight = 64.dp)
                                            .fillMaxHeight()
                                    )
                                }
                                if (artist.character?.aniListId != null) {
                                    SharedElement(
                                        key = "anime_character_${artist.character.aniListId}_image",
                                        screenKey = screenKey
                                    ) {
                                        image(modifier.clickable {
                                            // TODO: Use navigation callback
                                            uriHandler.openUri(artist.character.link)
                                        })
                                    }
                                } else {
                                    image(modifier)
                                }
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
                                            R.string.anime_media_artist_no_image
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
                                    R.string.anime_media_details_song_artist_as_character,
                                    artist.character.name(),
                                    artist.name,
                                )
                            } else {
                                stringResource(
                                    R.string.anime_media_details_song_artist_with_character,
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
