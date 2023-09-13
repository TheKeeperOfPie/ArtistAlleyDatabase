package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText

@OptIn(ExperimentalMaterial3Api::class)
object StaffStaffScreen {

    @Composable
    operator fun invoke(
        screenKey: String,
        staffTimeline: StaffDetailsViewModel.StaffTimeline,
    ) {
        val viewModel = hiltViewModel<StaffDetailsViewModel>()
        val viewer by viewModel.viewer.collectAsState()
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            staffTimeline.yearsToMedia.forEach { (year, entries) ->
                item {
                    viewModel.onRequestStaffYear(year)
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
                        items(entries, { it.id }) { entry ->
                            MediaGridCard(
                                screenKey = screenKey,
                                entry = entry,
                                viewer = viewer,
                                onClickListEdit = editViewModel::initialize,
                                showTypeIcon = true,
                                modifier = Modifier.width(120.dp),
                            ) { textColor ->
                                entry.role?.let {
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
                                            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
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
                            viewModel.onRequestStaffYear(
                                staffTimeline.yearsToMedia.lastOrNull()?.first
                            )
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
