package com.thekeeperofpie.artistalleydatabase.alley.changelog.favorites

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_artists_changelog_title
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.changelog.ArtistChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.changelog.StampRallyChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import org.jetbrains.compose.resources.stringResource

object FavoriteArtistsChangelogScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        dataYear: DataYear,
        onClickBack: () -> Unit,
        onClickArtist: (ArtistChangelogEntry) -> Unit,
        onClickStampRally: (StampRallyChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickArtistImage: (ArtistChangelogEntry, CatalogImage) -> Unit,
        onClickStampRallyImage: (StampRallyChangelogEntry, CatalogImage) -> Unit,
        viewModel: FavoriteArtistsChangelogViewModel = viewModel {
            graph.favoriteArtistsChangelogViewModelFactory.create(
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
            onClickArtist = onClickArtist,
            onClickStampRally = onClickStampRally,
            onClickSeries = onClickSeries,
            onClickMerch = onClickMerch,
            onClickArtistImage = onClickArtistImage,
            onClickStampRallyImage = onClickStampRallyImage,
            title = { Text(stringResource(Res.string.alley_favorite_artists_changelog_title)) },
        )
    }
}
