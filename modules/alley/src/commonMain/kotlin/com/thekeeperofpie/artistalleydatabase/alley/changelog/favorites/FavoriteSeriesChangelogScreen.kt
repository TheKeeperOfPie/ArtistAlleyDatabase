package com.thekeeperofpie.artistalleydatabase.alley.changelog.favorites

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_series_changelog_title
import com.thekeeperofpie.artistalleydatabase.alley.changelog.ArtistChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.changelog.StampRallyChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import org.jetbrains.compose.resources.stringResource

object FavoriteSeriesChangelogScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        onClickBack: () -> Unit,
        onClickArtist: (ArtistChangelogEntry) -> Unit,
        onClickStampRally: (StampRallyChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickArtistImage: (ArtistChangelogEntry, CatalogImage) -> Unit,
        onClickStampRallyImage: (StampRallyChangelogEntry, CatalogImage) -> Unit,
        viewModel: FavoriteSeriesChangelogViewModel = assistedMetroViewModel<FavoriteSeriesChangelogViewModel, FavoriteSeriesChangelogViewModel.Factory> {
            create(
                dataYear = dataYear,
                savedStateHandle = it.createSavedStateHandle(),
            )
        },
    ) {
        val seriesTitles by viewModel.seriesEntryCache.series.collectAsStateWithLifecycle()
        FavoritesChangelogScreen(
            dataYear = dataYear,
            changes = viewModel::changes,
            seriesTitles = { seriesTitles },
            showOnlyConfirmedTags = viewModel::showOnlyConfirmedTags,
            onChangeShowOnlyConfirmedTags = { viewModel.showOnlyConfirmedTags = it },
            onClickBack = onClickBack,
            onClickArtist = onClickArtist,
            onClickStampRally = onClickStampRally,
            onClickSeries = onClickSeries,
            onClickMerch = onClickMerch,
            onClickArtistImage = onClickArtistImage,
            onClickStampRallyImage = onClickStampRallyImage,
            title = { Text(stringResource(Res.string.alley_favorite_series_changelog_title)) },
        )
    }
}
