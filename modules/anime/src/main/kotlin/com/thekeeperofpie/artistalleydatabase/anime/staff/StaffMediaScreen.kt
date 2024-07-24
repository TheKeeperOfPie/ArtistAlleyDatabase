package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSmallCard
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@OptIn(ExperimentalMaterial3Api::class)
object StaffMediaScreen {

    @Composable
    operator fun invoke(
        mediaTimeline: StaffDetailsViewModel.MediaTimeline,
        onRequestYear: (Int?) -> Unit,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            mediaTimeline.yearsToCharacters.forEach { (year, entries) ->
                item {
                    onRequestYear(year)
                    Text(
                        text = year?.toString()
                            ?: stringResource(R.string.anime_staff_media_year_unknown),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
                    )
                }

                item {
                    val navigationCallback = LocalNavigationCallback.current
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(entries, { it.id }) {
                            val imageState = rememberCoilImageState(it.character.image?.large)
                            val innerImageState = rememberCoilImageState(it.media?.coverImage?.extraLarge)
                            val colorCalculationState = LocalColorCalculationState.current
                            val languageOptionMedia = LocalLanguageOptionMedia.current
                            val characterName = it.character.name?.primaryName()
                            val mediaSharedTransitionKey = it.media?.id?.toString()
                                ?.let { SharedTransitionKey.makeKeyForId(it) }
                            CharacterSmallCard(
                                screenKey = AnimeNavDestinations.STAFF_DETAILS.id,
                                id = EntryId("anime_character", it.character.id.toString()),
                                image = imageState.uri,
                                imageState = imageState,
                                innerImage = innerImageState.uri,
                                innerImageState = innerImageState,
                                innerImageKey = "anime_media_${it.media?.id}_image",
                                onClick = {
                                    navigationCallback.navigate(
                                        AnimeDestinations.CharacterDetails(
                                            characterId = it.character.id.toString(),
                                            headerParams = CharacterHeaderParams(
                                                name = characterName,
                                                subtitle = null,
                                                favorite = null,
                                                coverImage = imageState.toImageState(),
                                                colorArgb = colorCalculationState
                                                    .getColorsNonComposable(it.character.id.toString())
                                                    .first.toArgb(),
                                            )
                                        )
                                    )
                                },
                                onClickInnerImage = {
                                    if (it.media != null) {
                                        navigationCallback.navigate(
                                            AnimeDestinations.MediaDetails(
                                                mediaNavigationData = it.media,
                                                coverImage = innerImageState.toImageState(),
                                                languageOptionMedia = languageOptionMedia,
                                                sharedTransitionKey = mediaSharedTransitionKey,
                                            )
                                        )
                                    }
                                },
                            ) { textColor ->
                                it.role?.let {
                                    AutoHeightText(
                                        text = stringResource(it.toTextRes()),
                                        color = textColor,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            lineBreak = LineBreak(
                                                strategy = LineBreak.Strategy.Simple,
                                                strictness = LineBreak.Strictness.Strict,
                                                wordBreak = LineBreak.WordBreak.Default,
                                            )
                                        ),
                                        maxLines = 1,
                                        minTextSizeSp = 8f,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                                    )
                                }

                                it.character.name?.primaryName()?.let {
                                    AutoHeightText(
                                        text = it,
                                        color = textColor,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            lineBreak = LineBreak(
                                                strategy = LineBreak.Strategy.Balanced,
                                                strictness = LineBreak.Strictness.Strict,
                                                wordBreak = LineBreak.WordBreak.Default,
                                            )
                                        ),
                                        minTextSizeSp = 8f,
                                        minLines = 2,
                                        maxLines = 2,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            when (val loadMoreState = mediaTimeline.loadMoreState) {
                is StaffDetailsViewModel.MediaTimeline.LoadMoreState.Error -> item {
                    ErrorRow(
                        loadMoreState.throwable,
                        onClick = {
                            onRequestYear(mediaTimeline.yearsToCharacters.lastOrNull()?.first)
                        }
                    )
                }
                StaffDetailsViewModel.MediaTimeline.LoadMoreState.Loading ->
                    item { LoadingRow() }
                StaffDetailsViewModel.MediaTimeline.LoadMoreState.None -> Unit
            }
        }
    }

    @Composable
    fun LoadingRow() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            CircularProgressIndicator(modifier = Modifier.padding(32.dp))
        }
    }

    @Composable
    fun ErrorRow(throwable: Throwable, onClick: () -> Unit) {
        ElevatedCard(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.anime_staff_details_media_load_retry),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}
