package com.thekeeperofpie.artistalleydatabase.alley.rallies.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AssistedInject
class StampRallyMapViewModel(
    stampRallyEntryDao: StampRallyEntryDao,
    @Assisted route: AlleyDestination.StampRallyMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var stampRally by mutableStateOf<StampRallyDatabaseEntry?>(null)
        private set
    var artistTables by mutableStateOf(emptySet<String>())
        private set

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            val entry = stampRallyEntryDao.getEntryWithArtists(route.year, route.id)!!
            val tables = entry.artists
                .mapNotNull { it.booth }
                .toSet()
            withContext(CustomDispatchers.Main) {
                stampRally = entry.stampRally.stampRally
                artistTables = tables
            }
        }
    }


    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            route: AlleyDestination.StampRallyMap,
            savedStateHandle: SavedStateHandle,
        ): StampRallyMapViewModel
    }
}
