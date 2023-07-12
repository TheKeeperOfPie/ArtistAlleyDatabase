package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.MediaAndCharactersQuery
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(ExperimentalMaterial3Api::class)
object CharactersScreen {

    private val MIN_IMAGE_HEIGHT = 100.dp
    private val IMAGE_WIDTH = 72.dp
    private val SCREEN_KEY = AnimeNavDestinations.MEDIA_CHARACTERS.id

    @Composable
    operator fun invoke(
        viewModel: CharactersViewModel,
        headerValues: MediaHeaderValues,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val entry = viewModel.entry
        val media = entry?.media
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            topBar = {
                CollapsingToolbar(
                    maxHeight = 356.dp,
                    pinnedHeight = 120.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    MediaHeader(
                        screenKey = SCREEN_KEY,
                        mediaId = viewModel.mediaId,
                        titles = entry?.titlesUnique,
                        averageScore = media?.averageScore,
                        popularity = media?.popularity,
                        progress = it,
                        headerValues = headerValues,
                        colorCalculationState = colorCalculationState,
                        enableCoverImageSharedElement = false,
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
        ) {
            val characters = viewModel.characters.collectAsLazyPagingItems()
            LazyVerticalGrid(
                columns = GridCells.Adaptive(350.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(it)
            ) {
                item("header") {
                    DetailsSectionHeader(text = stringResource(R.string.anime_characters_header))
                }

                items(
                    count = characters.itemCount,
                    key = characters.itemKey { it.id },
                    contentType = characters.itemContentType { "character" },
                ) {
                    CharacterCard(
                        character = characters[it],
                        navigationCallback = navigationCallback,
                    )
                }
            }
        }
    }

    @Composable
    private fun CharacterCard(
        character: DetailsCharacter?,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        var imageWidthToHeightRatio by remember { MutableSingle(1f) }
        ElevatedCard(
            onClick = {
                character?.character?.let {
                    navigationCallback.onCharacterClick(it, imageWidthToHeightRatio)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .heightIn(min = MIN_IMAGE_HEIGHT)
            ) {
                val density = LocalDensity.current
                val width = density.run { IMAGE_WIDTH.roundToPx() }
                SharedElement(
                    key = "anime_character_${character?.id}_image",
                    screenKey = SCREEN_KEY,
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(character?.image)
                            .crossfade(true)
                            .allowHardware(true)
                            .size(width = Dimension.Pixels(width), height = Dimension.Undefined)
                            .build(),
                        contentScale = ContentScale.Crop,
                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                        contentDescription = stringResource(
                            R.string.anime_character_image_content_description
                        ),
                        onSuccess = { imageWidthToHeightRatio = it.widthToHeightRatio() },
                        modifier = Modifier
                            .width(IMAGE_WIDTH)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                            .placeholder(
                                visible = character == null,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )
                }

                val voiceActor = (character?.languageToVoiceActor?.get("Japanese")
                    ?: character?.languageToVoiceActor?.values?.firstOrNull())

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = character?.name ?: "FirstName LastName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.placeholder(
                                visible = character == null,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                        )

                        if (character == null || character.roleTextRes != null) {
                            Text(
                                text = stringResource(
                                    character?.roleTextRes ?: R.string.anime_character_role_main
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.placeholder(
                                    visible = character == null,
                                    highlight = PlaceholderHighlight.shimmer(),
                                )
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    if (character == null || voiceActor != null) {
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier
                                .wrapContentWidth()
                                .align(Alignment.End)
                        ) {
                            AutoSizeText(
                                text = voiceActor?.name ?: "FirstName LastName",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .placeholder(
                                        visible = character == null,
                                        highlight = PlaceholderHighlight.shimmer(),
                                    )
                            )
                        }
                    }
                }

                if (character == null || voiceActor?.image != null) {
                    var voiceActorImageWidthToHeightRatio by remember { MutableSingle(1f) }
                    SharedElement(
                        key = "anime_staff_${voiceActor?.id}_image",
                        screenKey = SCREEN_KEY,
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(voiceActor?.image)
                                .crossfade(true)
                                .allowHardware(true)
                                .size(width = Dimension.Pixels(width), height = Dimension.Undefined)
                                .build(),
                            contentScale = ContentScale.Crop,
                            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                            contentDescription = stringResource(
                                R.string.anime_media_voice_actor_image
                            ),
                            onSuccess = {
                                voiceActorImageWidthToHeightRatio = it.widthToHeightRatio()
                            },
                            modifier = Modifier
                                .width(IMAGE_WIDTH)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                                .placeholder(
                                    visible = character == null,
                                    highlight = PlaceholderHighlight.shimmer(),
                                )
                                .clickable {
                                    if (voiceActor != null) {
                                        navigationCallback.onStaffClick(
                                            voiceActor.staff,
                                            voiceActorImageWidthToHeightRatio,
                                        )
                                    }
                                }
                        )
                    }
                }
            }
        }
    }

    data class Entry(
        val media: MediaAndCharactersQuery.Data.Media,
    ) {
        val titlesUnique = media.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()

        val color = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)
    }

    data class CharacterEntry(
        val character: MediaAndCharactersQuery.Data.Media.Characters.Edge,
    )
}
