package com.thekeeperofpie.artistalleydatabase.alley.artist.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.math.absoluteValue
import kotlin.random.Random

@Inject
class ArtistMapViewModel(
    artistEntryDao: ArtistEntryDao,
    userEntryDao: UserEntryDao,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toDestination<Destinations.ArtistMap>(navigationTypeMap)
    val id = route.id

    var artist by mutableStateOf<ArtistEntryGridModel?>(null)
        private set

    private val randomSeed = savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }
    private val mutationUpdates = MutableSharedFlow<ArtistUserEntry>(5, 5)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            // Need to observe updates since it's possible to
            // toggle favorite from inside the map
            artistEntryDao.getEntryFlow(id)
                .map { ArtistEntryGridModel.buildFromEntry(randomSeed, it) }
                .flowOn(CustomDispatchers.IO)
                .collectLatest {
                    withContext(CustomDispatchers.Main) {
                        artist = it
                    }
                }
        }
        viewModelScope.launch(CustomDispatchers.IO) {
            mutationUpdates.collectLatest {
                userEntryDao.insertArtistUserEntry(it)
            }
        }
    }

    fun onFavoriteToggle(entry: ArtistEntryGridModel, favorite: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(favorite = favorite))
    }
}
