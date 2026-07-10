package com.thekeeperofpie.artistalleydatabase.alley.metrics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_metrics_artist_most_merch
import artistalleydatabase.modules.alley.generated.resources.alley_metrics_artist_most_rallies
import artistalleydatabase.modules.alley.generated.resources.alley_metrics_artist_most_series
import artistalleydatabase.modules.alley.generated.resources.alley_metrics_include_inferred
import artistalleydatabase.modules.alley.generated.resources.alley_metrics_no_data
import artistalleydatabase.modules.alley.generated.resources.alley_metrics_popular_merch
import artistalleydatabase.modules.alley.generated.resources.alley_metrics_popular_series
import artistalleydatabase.modules.alley.generated.resources.alley_metrics_title
import coil3.compose.AsyncImage
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortAndFilterComposables
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MetricsScreen(
    graph: ArtistAlleyGraph,
    onNavigateBack: () -> Unit,
    onClickArtist: (MetricsDao.ArtistData) -> Unit,
    onClickSeries: (MetricsDao.SeriesData) -> Unit,
    onClickMerch: (MetricsDao.MerchData) -> Unit,
    viewModel: MetricsViewModel = viewModel { graph.metricsViewModel() },
) {
    val dataYearHeaderState = rememberDataYearHeaderState(viewModel.dataYear, null)
    val data by viewModel.data.collectAsStateWithLifecycle()
    val seriesTitles by viewModel.seriesEntryCache.series.collectAsStateWithLifecycle()
    var includeInferred by viewModel.includeInferred.collectAsMutableStateWithLifecycle()
    MetricsScreen(
        dataYearHeaderState,
        data = { data },
        includeInferred = { includeInferred },
        onIncludeInferredChange = { includeInferred = it },
        seriesTitles = seriesTitles,
        seriesImage = viewModel::seriesImage,
        onNavigateBack = onNavigateBack,
        onClickArtist = onClickArtist,
        onClickSeries = onClickSeries,
        onClickMerch = onClickMerch,
    )
}

@Composable
internal fun MetricsScreen(
    dataYearHeaderState: DataYearHeaderState,
    includeInferred: () -> Boolean,
    onIncludeInferredChange: (Boolean) -> Unit,
    data: () -> MetricsViewModel.Data,
    seriesTitles: Map<String, GetSeriesTitles>,
    seriesImage: (String) -> String?,
    onNavigateBack: () -> Unit,
    onClickArtist: (MetricsDao.ArtistData) -> Unit,
    onClickSeries: (MetricsDao.SeriesData) -> Unit,
    onClickMerch: (MetricsDao.MerchData) -> Unit,
) {
    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(Res.string.alley_metrics_title),
                    upIconOption = UpIconOption.Back(onNavigateBack),
                )
            },
            modifier = Modifier.widthIn(max = 1200.dp),
        ) {
            Column(modifier = Modifier.padding(it).verticalScroll(rememberScrollState())) {
                DataYearHeader(state = dataYearHeaderState)
                SortAndFilterComposables.SwitchRow(
                    title = Res.string.alley_metrics_include_inferred,
                    enabled = includeInferred,
                    onEnabledChanged = onIncludeInferredChange,
                    showDivider = false,
                )

                val data = data()
                HorizontalDivider()
                ArtistMetricsList(
                    title = Res.string.alley_metrics_artist_most_series,
                    artists = data.artistsBySeries,
                    onClickArtist = onClickArtist,
                )
                HorizontalDivider()
                ArtistMetricsList(
                    title = Res.string.alley_metrics_artist_most_merch,
                    artists = data.artistsByMerch,
                    onClickArtist = onClickArtist,
                )
                HorizontalDivider()
                ArtistMetricsList(
                    title = Res.string.alley_metrics_artist_most_rallies,
                    artists = data.artistsByRallies,
                    onClickArtist = onClickArtist,
                )
                HorizontalDivider()
                MetricsList(
                    title = Res.string.alley_metrics_popular_series,
                    values = data.series,
                ) { index, series ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClickSeries(series) }
                            .height(IntrinsicSize.Min)
                            .padding(horizontal = 8.dp)
                    ) {
                        IndexText(index)
                        AsyncImage(
                            model = seriesImage(series.id),
                            null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxHeight()
                                .width(56.dp)
                                .height(80.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .sharedElement("seriesImage", series.id)
                        )

                        Text(
                            text = seriesTitles[series.id]
                                ?.name(LocalLanguageOptionMedia.current)
                                ?: series.id,
                            modifier = Modifier.weight(1f)
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                        Text(
                            text = series.count.toString(),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                HorizontalDivider()
                MetricsList(
                    title = Res.string.alley_metrics_popular_merch,
                    values = data.merch,
                ) { index, merch ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClickMerch(merch) }
                            .height(IntrinsicSize.Min)
                            .padding(horizontal = 8.dp)
                    ) {
                        IndexText(index)
                        Text(
                            text = merch.id,
                            modifier = Modifier.weight(1f)
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                        Text(
                            text = merch.count.toString(),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> MetricsList(
    title: StringResource,
    values: List<T>,
    content: @Composable (index: Int, value: T) -> Unit,
) {
    Column {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (values.isEmpty()) {
            Text(
                text = stringResource(Res.string.alley_metrics_no_data),
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            values.forEachIndexed { index, value ->
                content(index, value)
            }
        }
    }
}

@Composable
private fun IndexText(index: Int) {
    Text(
        text = (index + 1).toString().padStart(2, ' ') + ".",
        style = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun ArtistMetricsList(
    title: StringResource,
    artists: List<MetricsDao.ArtistData>,
    onClickArtist: (MetricsDao.ArtistData) -> Unit,
) {
    MetricsList(
        title = title,
        values = artists,
    ) { index, artist ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClickArtist(artist) }
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            IndexText(index)
            Text(text = artist.name)
            Spacer(Modifier.weight(1f))
            Text(
                text = artist.count.toString(),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
