package com.thekeeperofpie.artistalleydatabase.alley

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistMerchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistSeriesViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.map.ArtistMapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.changelog.ArtistChangelogViewModel
import com.thekeeperofpie.artistalleydatabase.alley.changelog.FavoritesChangelogViewModel
import com.thekeeperofpie.artistalleydatabase.alley.changelog.MerchChangelogViewModel
import com.thekeeperofpie.artistalleydatabase.alley.changelog.SeriesChangelogViewModel
import com.thekeeperofpie.artistalleydatabase.alley.changelog.StampRallyChangelogViewModel
import com.thekeeperofpie.artistalleydatabase.alley.changelog.TagChangelogViewModel
import com.thekeeperofpie.artistalleydatabase.alley.export.QrCodeViewModel
import com.thekeeperofpie.artistalleydatabase.alley.favorite.FavoritesViewModel
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagesViewModel
import com.thekeeperofpie.artistalleydatabase.alley.import.ImportViewModel
import com.thekeeperofpie.artistalleydatabase.alley.map.MapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.map.favorites.FavoritesSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.details.StampRallyDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.map.StampRallyMapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.settings.AboutLibrariesProvider
import com.thekeeperofpie.artistalleydatabase.alley.settings.AlleyAboutLibrariesProvider
import com.thekeeperofpie.artistalleydatabase.alley.settings.AlleySettingsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapViewModel
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides

interface ArtistAlleyGraph {

    val artistDetailsViewModelFactory: ArtistDetailsViewModel.Factory
    val artistMapViewModelFactory: ArtistMapViewModel.Factory
    val artistMerchViewModelFactory: ArtistMerchViewModel.Factory
    val artistSearchViewModelFactory: ArtistSearchViewModel.Factory
    val artistSeriesViewModelFactory: ArtistSeriesViewModel.Factory
    val artistChangelogViewModelFactory: ArtistChangelogViewModel.Factory
    val favoritesChangelogViewModelFactory: FavoritesChangelogViewModel.Factory
    val favoritesViewModelFactory: FavoritesViewModel.Factory
    val favoritesSortFilterViewModelFactory: FavoritesSortFilterViewModel.Factory
    val imagesViewModel: Provider<ImagesViewModel>
    val importViewModelFactory: ImportViewModel.Factory
    val mapViewModelFactory: MapViewModel.Factory
    val merchChangelogViewModelFactory: () -> MerchChangelogViewModel
    val seriesChangelogViewModelFactory: () -> SeriesChangelogViewModel
    val stampRallyChangelogViewModelFactory: StampRallyChangelogViewModel.Factory
    val stampRallyDetailsViewModelFactory: StampRallyDetailsViewModel.Factory
    val stampRallyMapViewModelFactory: StampRallyMapViewModel.Factory
    val stampRallySearchViewModelFactory: StampRallySearchViewModel.Factory
    val tagChangelogViewModelFactory: TagChangelogViewModel.Factory
    val tagMapViewModelFactory: TagMapViewModel.Factory
    val tagsViewModelFactory: TagsViewModel.Factory

    val alleySettingsViewModel: Provider<AlleySettingsViewModel>
    val qrCodeViewModel: Provider<QrCodeViewModel>

    val settings: ArtistAlleySettings
    val aboutLibrariesProviders: Set<AboutLibrariesProvider>

    @IntoSet
    @Provides
    fun provideAlleyAboutLibrariesProvider(): AboutLibrariesProvider = AlleyAboutLibrariesProvider
}
