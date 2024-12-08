package com.thekeeperofpie.artistalleydatabase.anime.staff.details

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.staff.generated.resources.Res
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_details_media_load_retry
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_media_year_unknown
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import org.jetbrains.compose.resources.stringResource

object StaffStaffScreen {

    @Composable
    operator fun <MediaEntry> invoke(
        staffTimeline: StaffTimeline<MediaEntry>,
        onRequestStaffYear: (Int?) -> Unit,
        mediaGridCard: @Composable (StaffTimeline.MediaWithRole<MediaEntry>) -> Unit,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            staffTimeline.yearsToMedia.forEach { (year, entries) ->
                item {
                    SideEffect { onRequestStaffYear(year) }
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
                                mediaGridCard(entry)
                            }
                        }
                    }
                }
            }

            when (val loadMoreState = staffTimeline.loadMoreState) {
                is StaffTimeline.LoadMoreState.Error -> item {
                    ErrorRow(
                        loadMoreState.throwable,
                        onClick = {
                            // TODO: This doesn't actually work, need to signal a manual override
                            onRequestStaffYear(
                                staffTimeline.yearsToMedia.lastOrNull()?.first
                            )
                        }
                    )
                }
                StaffTimeline.LoadMoreState.Loading ->
                    item { LoadingRow() }
                StaffTimeline.LoadMoreState.None -> Unit
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
