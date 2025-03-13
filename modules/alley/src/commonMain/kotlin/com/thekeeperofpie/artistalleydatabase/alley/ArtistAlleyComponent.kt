package com.thekeeperofpie.artistalleydatabase.alley

import androidx.lifecycle.SavedStateHandle
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.map.ArtistMapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.database.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.database.NotesDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.favorite.FavoritesViewModel
import com.thekeeperofpie.artistalleydatabase.alley.map.MapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.map.favorites.FavoritesSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.details.StampRallyDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.map.StampRallyMapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.settings.SettingsViewModel
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
    val artistSearchViewModel: (SavedStateHandle, StateFlow<ArtistSortFilterViewModel.FilterParams>) -> ArtistSearchViewModel
    val artistSortFilterViewModel: (SavedStateHandle) -> ArtistSortFilterViewModel
    val favoritesViewModel: (
        SavedStateHandle,
        StateFlow<ArtistSortFilterViewModel.FilterParams>,
        StateFlow<StampRallySortFilterViewModel.FilterParams>,
    ) -> FavoritesViewModel
    val favoritesSortFilterViewModel: (SavedStateHandle) -> FavoritesSortFilterViewModel
    val mapViewModel: (SavedStateHandle) -> MapViewModel
    val settingsViewModel: () -> SettingsViewModel
    val stampRallyDetailsViewModel: (SavedStateHandle) -> StampRallyDetailsViewModel
    val stampRallyMapViewModel: (SavedStateHandle) -> StampRallyMapViewModel
    val stampRallySearchViewModel: (StateFlow<StampRallySortFilterViewModel.FilterParams>) -> StampRallySearchViewModel
    val stampRallySortFilterViewModel: (SavedStateHandle) -> StampRallySortFilterViewModel
    val tagMapViewModel: (SavedStateHandle) -> TagMapViewModel
    val tagsViewModel: () -> TagsViewModel

    val ArtistAlleyDatabase.bindArtistEntryDao: ArtistEntryDao
        @Provides get() = this.artistEntryDao

    val ArtistAlleyDatabase.bindStampRallyEntryDao: StampRallyEntryDao
        @Provides get() = this.stampRallyEntryDao

    val ArtistAlleyDatabase.bindNotesDao: NotesDao
        @Provides get() = this.notesDao

    val ArtistAlleyDatabase.bindTagEntryDao: TagEntryDao
        @Provides get() = this.tagEntryDao

    val ArtistAlleyDatabase.bindUserEntryDao: UserEntryDao
        @Provides get() = this.userEntryDao

    val navigationTypeMap: NavigationTypeMap
    val settings: ArtistAlleySettings

    @SingletonScope
    @Provides
    fun provideNavigationTypeMap() = NavigationTypeMap(Destinations.typeMap)
}
