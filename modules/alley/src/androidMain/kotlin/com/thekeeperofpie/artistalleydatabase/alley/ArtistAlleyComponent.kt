package com.thekeeperofpie.artistalleydatabase.alley

import androidx.lifecycle.SavedStateHandle
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.map.ArtistMapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.map.MapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.map.favorites.FavoritesSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.details.StampRallyDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.map.StampRallyMapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapViewModel
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Provides

interface ArtistAlleyComponent {

    val appUpdateViewModel: () -> AppUpdateViewModel
    val artistDetailsViewModel: (SavedStateHandle) -> ArtistDetailsViewModel
    val artistSearchViewModel: (SavedStateHandle, StateFlow<ArtistSortFilterViewModel.FilterParams>) -> ArtistSearchViewModel
    val artistSortFilterViewModel: (SavedStateHandle) -> ArtistSortFilterViewModel
    val tagsViewModel: () -> TagsViewModel
    val artistMapViewModel: (SavedStateHandle) -> ArtistMapViewModel
    val mapViewModel: () -> MapViewModel
    val stampRallyDetailsViewModel: (SavedStateHandle) -> StampRallyDetailsViewModel
    val stampRallySortFilterViewModel: (SavedStateHandle) -> StampRallySortFilterViewModel
    val stampRallySearchViewModel: (StateFlow<StampRallySortFilterViewModel.FilterParams>) -> StampRallySearchViewModel
    val stampRallyMapViewModel: (SavedStateHandle) -> StampRallyMapViewModel
    val tagMapViewModel: (SavedStateHandle) -> TagMapViewModel
    val favoritesSortFilterViewModel: () -> FavoritesSortFilterViewModel

    @SingletonScope
    @Provides
    fun provideArtistEntryDao(database: ArtistAlleyDatabase) = database.artistEntryDao()

    @SingletonScope
    @Provides
    fun provideStampRallyEntryDao(database: ArtistAlleyDatabase) = database.stampRallyEntryDao()

    @SingletonScope
    @Provides
    fun provideTagEntryDao(database: ArtistAlleyDatabase) = database.tagEntryDao()
}
