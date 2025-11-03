package com.thekeeperofpie.artistalleydatabase.alley.rallies.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AssistedInject
class StampRallyMapViewModel(
    stampRallyEntryDao: StampRallyEntryDao,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toDestination<Destinations.StampRallyMap>(navigationTypeMap)

    var stampRally by mutableStateOf<StampRallyEntry?>(null)
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
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): StampRallyMapViewModel
    }
}
