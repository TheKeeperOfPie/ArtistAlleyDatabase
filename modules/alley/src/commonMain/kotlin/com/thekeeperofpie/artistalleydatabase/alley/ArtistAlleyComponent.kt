package com.thekeeperofpie.artistalleydatabase.alley

import androidx.lifecycle.SavedStateHandle
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
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.AlleySettingsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapViewModel
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Provides

interface ArtistAlleyComponent {

    val artistDetailsViewModel: (SavedStateHandle) -> ArtistDetailsViewModel
    val artistMapViewModel: (SavedStateHandle) -> ArtistMapViewModel
    val artistMerchViewModel: (SavedStateHandle) -> ArtistMerchViewModel
    val artistSearchViewModel: (SavedStateHandle) -> ArtistSearchViewModel
    val artistSeriesViewModel: (SavedStateHandle) -> ArtistSeriesViewModel
    val favoritesViewModel: (
        SavedStateHandle,
        StateFlow<StampRallySortFilterViewModel.FilterParams>,
    ) -> FavoritesViewModel
    val favoritesSortFilterViewModel: (SavedStateHandle) -> FavoritesSortFilterViewModel
    val imagesViewModel: (SavedStateHandle) -> ImagesViewModel
    val importViewModel: (SavedStateHandle) -> ImportViewModel
    val mapViewModel: (SavedStateHandle) -> MapViewModel
    val alleySettingsViewModel: () -> AlleySettingsViewModel
    val qrCodeViewModel: () -> QrCodeViewModel
    val stampRallyDetailsViewModel: (SavedStateHandle) -> StampRallyDetailsViewModel
    val stampRallyMapViewModel: (SavedStateHandle) -> StampRallyMapViewModel
    val stampRallySearchViewModel: (
        SavedStateHandle,
        StateFlow<StampRallySortFilterViewModel.FilterParams>,
    ) -> StampRallySearchViewModel
    val stampRallySortFilterViewModel: (SavedStateHandle) -> StampRallySortFilterViewModel
    val tagMapViewModel: (SavedStateHandle) -> TagMapViewModel
    val tagsViewModel: (SavedStateHandle) -> TagsViewModel

    val ArtistAlleyDatabase.bindArtistEntryDao: ArtistEntryDao
        @Provides get() = this.artistEntryDao

    val ArtistAlleyDatabase.bindStampRallyEntryDao: StampRallyEntryDao
        @Provides get() = this.stampRallyEntryDao

    val ArtistAlleyDatabase.bindImportExportDao: ImportExportDao
        @Provides get() = this.importExportDao

    val ArtistAlleyDatabase.bindImageEntryDao: ImageEntryDao
        @Provides get() = this.imageEntryDao

    val ArtistAlleyDatabase.bindMerchEntryDao: MerchEntryDao
        @Provides get() = this.merchEntryDao

    val ArtistAlleyDatabase.bindSeriesEntryDao: SeriesEntryDao
        @Provides get() = this.seriesEntryDao

    val ArtistAlleyDatabase.bindTagEntryDao: TagEntryDao
        @Provides get() = this.tagEntryDao

    val ArtistAlleyDatabase.bindUserEntryDao: UserEntryDao
        @Provides get() = this.userEntryDao

    val ArtistAlleyDatabase.bindNotesDao: UserNotesDao
        @Provides get() = this.userNotesDao

    val navigationTypeMap: NavigationTypeMap
    val settings: ArtistAlleySettings

    @SingletonScope
    @Provides
    fun provideNavigationTypeMap() = NavigationTypeMap(Destinations.typeMap)
}
