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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_staff_details_media_load_retry
import artistalleydatabase.modules.anime.generated.resources.anime_staff_media_year_unknown
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import org.jetbrains.compose.resources.stringResource

object StaffStaffScreen {

    @Composable
    operator fun invoke(staffTimeline: StaffDetailsViewModel.StaffTimeline) {
        val animeComponent = LocalAnimeComponent.current
        val viewModel = viewModel { animeComponent.staffDetailsViewModel(createSavedStateHandle()) }
        val viewer by viewModel.viewer.collectAsState()
        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            staffTimeline.yearsToMedia.forEach { (year, entries) ->
                item {
                    viewModel.onRequestStaffYear(year)
                    Text(
                        text = year?.toString()
                            ?: stringResource(Res.string.anime_staff_media_year_unknown),
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
                            SharedTransitionKeyScope(
                                "staff_staff_card",
                                year?.toString(),
                                entry.id,
                            ) {
                                MediaGridCard(
                                    entry = entry,
                                    viewer = viewer,
                                    onClickListEdit = editViewModel::initialize,
                                    modifier = Modifier.width(120.dp),
                                    showTypeIcon = true,
                                ) { textColor ->
                                    entry.role?.let {
                                        AutoHeightText(
                                            text = it,
                                            color = textColor,
                                            style = MaterialTheme.typography.bodySmall
                                                .copy(lineBreak = LineBreak.Heading),
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
                text = stringResource(Res.string.anime_staff_details_media_load_retry),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}
