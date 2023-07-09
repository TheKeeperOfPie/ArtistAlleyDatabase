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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterCard
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@OptIn(ExperimentalMaterial3Api::class)
object StaffStaffScreen {

    @Composable
    operator fun invoke(
        staffTimeline: StaffDetailsViewModel.StaffTimeline,
        onRequestYear: (Int?) -> Unit,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            staffTimeline.yearsToMedia.forEach { (year, entries) ->
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
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(entries, { it.id }) {
                            var imageWidthToHeightRatio by remember { MutableSingle(1f) }
                            var innerImageWidthToHeightRatio by remember { MutableSingle(1f) }
                            CharacterCard(
                                screenKey = AnimeNavDestinations.STAFF_DETAILS.id,
                                id = EntryId("anime_media", it.media.id.toString()),
                                image = it.media.coverImage?.extraLarge,
                                colorCalculationState = colorCalculationState,
                                innerImage = it.character?.image?.large,
                                onImageSuccess = {
                                    imageWidthToHeightRatio = it.widthToHeightRatio()
                                },
                                onInnerImageSuccess = {
                                    innerImageWidthToHeightRatio = it.widthToHeightRatio()
                                },
                                onClick = {
                                    navigationCallback.onMediaClick(
                                        it.media,
                                        imageWidthToHeightRatio
                                    )
                                },
                                onClickInnerImage = {
                                    it.character?.let {
                                        navigationCallback.onCharacterClick(
                                            it,
                                            innerImageWidthToHeightRatio
                                        )
                                    }
                                },
                                width = 140.dp,
                            ) { textColor ->
                                it.role?.let {
                                    AutoHeightText(
                                        text = it,
                                        color = textColor,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            lineBreak = LineBreak(
                                                strategy = LineBreak.Strategy.Simple,
                                                strictness = LineBreak.Strictness.Strict,
                                                wordBreak = LineBreak.WordBreak.Default,
                                            )
                                        ),
                                        minLines = 2,
                                        maxLines = 2,
                                        minTextSizeSp = 8f,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                                    )
                                }

                                it.media.title?.userPreferred?.let {
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
                                        minLines = 3,
                                        maxLines = 3,
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

            when (val loadMoreState = staffTimeline.loadMoreState) {
                is StaffDetailsViewModel.StaffTimeline.LoadMoreState.Error -> item {
                    ErrorRow(
                        loadMoreState.throwable,
                        onClick = {
                            // TODO: This doesn't actually work, need to signal a manual override
                            onRequestYear(staffTimeline.yearsToMedia.lastOrNull()?.first)
                        }
                    )
                }
                StaffDetailsViewModel.StaffTimeline.LoadMoreState.Loading ->
                    item { LoadingRow() }
                StaffDetailsViewModel.StaffTimeline.LoadMoreState.None -> Unit
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
