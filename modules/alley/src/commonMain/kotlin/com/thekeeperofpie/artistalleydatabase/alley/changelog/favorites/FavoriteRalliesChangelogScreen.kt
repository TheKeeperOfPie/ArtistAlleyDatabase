package com.thekeeperofpie.artistalleydatabase.alley.changelog.favorites

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_rallies_changelog_title
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.changelog.StampRallyChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import org.jetbrains.compose.resources.stringResource

object FavoriteRalliesChangelogScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        dataYear: DataYear,
        onClickBack: () -> Unit,
        onClickStampRally: (StampRallyChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickStampRallyImage: (StampRallyChangelogEntry, CatalogImage) -> Unit,
        viewModel: FavoriteRalliesChangelogViewModel = viewModel {
            graph.favoriteRalliesChangelogViewModelFactory.create(
                dataYear = dataYear,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        val changes by viewModel.changes.collectAsStateWithLifecycle()
        val seriesTitles by viewModel.seriesEntryCache.series.collectAsStateWithLifecycle()
        FavoritesChangelogScreen(
            dataYear = dataYear,
            changes = { changes },
            seriesTitles = { seriesTitles },
            seriesImage = viewModel::seriesImage,
            showOnlyConfirmedTags = null,
            onChangeShowOnlyConfirmedTags = {},
            onClickBack = onClickBack,
            onClickArtist = {},
            onClickStampRally = onClickStampRally,
            onClickSeries = onClickSeries,
            onClickMerch = onClickMerch,
            onClickArtistImage = { _, _ -> },
            onClickStampRallyImage = onClickStampRallyImage,
            title = { Text(stringResource(Res.string.alley_favorite_rallies_changelog_title)) },
        )
    }
}
