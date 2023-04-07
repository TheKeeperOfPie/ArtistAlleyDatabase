package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.ItemDropdown
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeMediaFilterOptionsBottomPanel {

    @Composable
    operator fun <SortOption : AnimeMediaFilterController.Data.SortOption> invoke(
        modifier: Modifier = Modifier,
        filterData: () -> AnimeMediaFilterController.Data<SortOption>,
        errorRes: () -> Int? = { null },
        exception: () -> Exception? = { null },
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val scaffoldState = rememberBottomSheetScaffoldState()
        LaunchedEffect(true) {
            // An initial value of HIDE crashes, so just hide it manually
            scaffoldState.bottomSheetState.hide()
        }

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                OptionsPanel(filterData = filterData)
            },
            snackbarHost = {
                @Suppress("NAME_SHADOWING")
                val errorRes = errorRes()
                if (errorRes != null) {
                    SnackbarErrorText(errorRes, exception())
                } else {
                    // Bottom sheet requires at least one measurable component
                    Spacer(modifier = Modifier.size(0.dp))
                }
            },
            modifier = modifier,
            content = content
        )
    }

    @Composable
    private fun <SortOption : AnimeMediaFilterController.Data.SortOption> OptionsPanel(
        filterData: () -> AnimeMediaFilterController.Data<SortOption>
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val data = filterData()

            ItemDropdown(
                label = R.string.anime_media_filter_sort_label,
                value = stringResource(data.sort().textRes),
                iconContentDescription = R.string.anime_media_filter_sort_dropdown_content_description,
                values = { filterData().defaultOptions },
                textForValue = { stringResource(it.textRes) },
                onSelectItem = { data.onSortChanged(it) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )

            // Default sort doesn't allow changing ascending
            if (data.sort() != filterData().defaultOptions.first()) {
                ItemDropdown(
                    label = R.string.anime_media_filter_sort_ascending_label,
                    value = ascendingText(data.sortAscending()),
                    iconContentDescription = R.string.anime_media_filter_sort_ascending_dropdown_content_description,
                    values = { listOf(true, false) },
                    textForValue = { ascendingText(it) },
                    onSelectItem = data.onSortAscendingChanged,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            GenreSection(
                genres = { filterData().genres() },
                onGenreClicked = filterData().onGenreClicked,
            )

            TagSection(
                tagsByCategory = { filterData().tagsByCategory() },
                onTagClicked = filterData().onTagClicked,
            )
        }
    }

    @Composable
    private fun ascendingText(ascending: Boolean) = stringResource(
        if (ascending) {
            R.string.anime_media_filter_sort_ascending
        } else {
            R.string.anime_media_filter_sort_descending
        }
    )

    @Composable
    private fun GenreSection(
        genres: @Composable () -> List<MediaGenreEntry>,
        onGenreClicked: (String) -> Unit
    ) {
        Text(
            text = stringResource(R.string.anime_media_filter_genre_label),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )

        @Suppress("NAME_SHADOWING")
        val genres = genres()
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fadingEdge()

        ) {
            items(genres, { it.name }) {
                FilterChip(
                    selected = it.state != MediaGenreEntry.State.DEFAULT,
                    onClick = { onGenreClicked(it.name) },
                    label = { Text(it.name) },
                    leadingIcon = {
                        when (it.state) {
                            MediaGenreEntry.State.DEFAULT,
                            MediaGenreEntry.State.INCLUDE -> Icons.Filled.Check
                            MediaGenreEntry.State.EXCLUDE -> Icons.Filled.Close
                        }.let {
                            Icon(
                                imageVector = it,
                                contentDescription = stringResource(
                                    R.string.anime_media_genre_chip_state_content_description
                                )
                            )
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun TagSection(
        tagsByCategory: @Composable () -> Map<String?, List<MediaTagEntry>>,
        onTagClicked: (String) -> Unit
    ) {
        var rootExpanded by remember { mutableStateOf(false) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { rootExpanded = !rootExpanded }
        ) {
            Text(
                text = stringResource(R.string.anime_media_filter_tag_label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .weight(1f)
            )
            TrailingDropdownIconButton(
                expanded = rootExpanded,
                contentDescription = stringResource(
                    R.string.anime_media_filter_tag_content_description
                ),
                onClick = { rootExpanded = !rootExpanded },
            )
        }

        AnimatedVisibility(
            visible = rootExpanded,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Column {
                @Suppress("NAME_SHADOWING")
                val tagsByCategory = tagsByCategory()
                tagsByCategory.forEach { (category, entries) ->
                    var categoryExpanded by remember { mutableStateOf(false) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = !categoryExpanded }
                    ) {
                        val categoryText =
                            category ?: stringResource(R.string.anime_media_filter_tag_misc)
                        Text(
                            text = categoryText,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 32.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                                .weight(1f)
                        )
                        TrailingDropdownIconButton(
                            expanded = categoryExpanded,
                            contentDescription = stringResource(
                                R.string.anime_media_filter_tag_category_expand_content_description,
                                categoryText
                            ),
                            onClick = { categoryExpanded = !categoryExpanded },
                        )
                    }

                    AnimatedVisibility(
                        visible = categoryExpanded,
                        enter = expandVertically(expandFrom = Alignment.Top),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 48.dp, end = 16.dp),
                        ) {
                            entries.forEach {
                                FilterChip(
                                    selected = it.state != MediaTagEntry.State.DEFAULT,
                                    onClick = { onTagClicked(it.id) },
                                    label = { Text(it.name ?: it.id) },
                                    leadingIcon = {
                                        when (it.state) {
                                            MediaTagEntry.State.DEFAULT,
                                            MediaTagEntry.State.INCLUDE -> Icons.Filled.Check
                                            MediaTagEntry.State.EXCLUDE -> Icons.Filled.Close
                                        }.let {
                                            Icon(
                                                imageVector = it,
                                                contentDescription = stringResource(
                                                    R.string.anime_media_tag_chip_state_content_description
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Modifier.fadingEdge() = composed {
        val localConfiguration = LocalConfiguration.current
        val colorStopTransparent = remember { (4.dp / localConfiguration.screenWidthDp.dp) }
        val colorStopOpaque = remember { (16.dp / localConfiguration.screenWidthDp.dp) }
        val fadingEdgeBrush = remember {
            Brush.horizontalGradient(
                colorStopTransparent to Color.Transparent,
                colorStopOpaque to Color.Black,
                (1f - colorStopOpaque) to Color.Black,
                (1f - colorStopTransparent) to Color.Transparent,
            )
        }

        graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawRect(
                        brush = fadingEdgeBrush,
                        blendMode = BlendMode.DstIn
                    )
                }
            }
    }
}

@Preview
@Composable
private fun Preview() {
    AnimeMediaFilterOptionsBottomPanel(
        filterData = { AnimeMediaFilterController.Data.forPreview<MediaSortOption>() }
    ) {}
}