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
import com.thekeeperofpie.artistalleydatabase.compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StampRallyMapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    stampRallyEntryDao: StampRallyEntryDao,
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {

    private val route = savedStateHandle.toDestination<Destinations.StampRallyMap>(navigationTypeMap)

    var stampRally by mutableStateOf<StampRallyEntry?>(null)
        private set
    var artistTables by mutableStateOf(emptySet<String>())
        private set

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            val entry = stampRallyEntryDao.getEntryWithArtists(route.id)!!
            val tables = entry.artists
                .map { it.booth }
                .toSet()
            withContext(CustomDispatchers.Main) {
                stampRally = entry.stampRally
                artistTables = tables
            }
        }
    }
}
