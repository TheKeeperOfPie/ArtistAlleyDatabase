package com.thekeeperofpie.artistalleydatabase.alley

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistMerchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistSeriesViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.map.ArtistMapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.database.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.database.ImportExportDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserNotesDao
import com.thekeeperofpie.artistalleydatabase.alley.export.QrCodeViewModel
import com.thekeeperofpie.artistalleydatabase.alley.favorite.FavoritesViewModel
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagesViewModel
import com.thekeeperofpie.artistalleydatabase.alley.import.ImportViewModel
import com.thekeeperofpie.artistalleydatabase.alley.map.MapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.map.favorites.FavoritesSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.details.StampRallyDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.map.StampRallyMapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.AlleySettingsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapViewModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

interface ArtistAlleyComponent {

    val artistDetailsViewModelFactory: ArtistDetailsViewModel.Factory
    val artistMapViewModelFactory: ArtistMapViewModel.Factory
    val artistMerchViewModelFactory: ArtistMerchViewModel.Factory
    val artistSearchViewModelFactory: ArtistSearchViewModel.Factory
    val artistSeriesViewModelFactory: ArtistSeriesViewModel.Factory
    val favoritesViewModelFactory: FavoritesViewModel.Factory
    val favoritesSortFilterViewModelFactory: FavoritesSortFilterViewModel.Factory
    val imagesViewModelFactory: ImagesViewModel.Factory
    val importViewModelFactory: ImportViewModel.Factory
    val mapViewModelFactory: MapViewModel.Factory
    val stampRallyDetailsViewModelFactory: StampRallyDetailsViewModel.Factory
    val stampRallyMapViewModelFactory: StampRallyMapViewModel.Factory
    val stampRallySearchViewModelFactory: StampRallySearchViewModel.Factory
    val tagMapViewModelFactory: TagMapViewModel.Factory
    val tagsViewModelFactory: TagsViewModel.Factory

    val alleySettingsViewModel: Provider<AlleySettingsViewModel>
    val qrCodeViewModel: Provider<QrCodeViewModel>

    @Provides
    fun bindArtistEntryDao(database: ArtistAlleyDatabase): ArtistEntryDao = database.artistEntryDao

    @Provides
    fun bindStampRallyEntryDao(database: ArtistAlleyDatabase): StampRallyEntryDao =
        database.stampRallyEntryDao

    @Provides
    fun bindImportExportDao(database: ArtistAlleyDatabase): ImportExportDao =
        database.importExportDao

    @Provides
    fun bindImageEntryDao(database: ArtistAlleyDatabase): ImageEntryDao = database.imageEntryDao

    @Provides
    fun bindMerchEntryDao(database: ArtistAlleyDatabase): MerchEntryDao = database.merchEntryDao

    @Provides
    fun bindSeriesEntryDao(database: ArtistAlleyDatabase): SeriesEntryDao = database.seriesEntryDao

    @Provides
    fun bindTagEntryDao(database: ArtistAlleyDatabase): TagEntryDao = database.tagEntryDao

    @Provides
    fun bindUserEntryDao(database: ArtistAlleyDatabase): UserEntryDao = database.userEntryDao

    @Provides
    fun bindUserNotesDao(database: ArtistAlleyDatabase): UserNotesDao = database.userNotesDao

    val navigationTypeMap: NavigationTypeMap
    val settings: ArtistAlleySettings

    @SingleIn(AppScope::class)
    @Provides
    fun provideNavigationTypeMap(): NavigationTypeMap = NavigationTypeMap(Destinations.typeMap)
}
