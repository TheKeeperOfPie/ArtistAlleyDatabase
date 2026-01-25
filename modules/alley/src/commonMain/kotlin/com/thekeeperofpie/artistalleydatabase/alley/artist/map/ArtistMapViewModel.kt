package com.thekeeperofpie.artistalleydatabase.alley.artist.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class ArtistMapViewModel(
    artistEntryDao: ArtistEntryDao,
    userEntryDao: UserEntryDao,
    settings: ArtistAlleySettings,
    @Assisted route: AlleyDestination.ArtistMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val id = route.id

    val artist = combine(settings.showOnlyConfirmedTags, settings.showOutdatedCatalogs, ::Pair)
        .flatMapLatest { (showOnlyConfirmedTags, showOutdatedCatalogs) ->
            // Need to observe updates since it's possible to
            // toggle favorite from inside the map
            artistEntryDao.getEntryFlow(id)
                .map {
                    ArtistEntryGridModel.buildFromEntry(
                        randomSeed = randomSeed,
                        showOnlyConfirmedTags = showOnlyConfirmedTags,
                        entry = it,
                        showOutdatedCatalogs = showOutdatedCatalogs,
                        fallbackCatalog = artistEntryDao.getFallbackImages(it.artist),
                    )
                }
        }
        .flowOn(CustomDispatchers.IO)
        .stateInForCompose(this, null)

    private val randomSeed =
        savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }
    private val mutationUpdates = MutableSharedFlow<ArtistUserEntry>(5, 5)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            mutationUpdates.collectLatest {
                userEntryDao.insertArtistUserEntry(it)
            }
        }
    }

    fun onFavoriteToggle(entry: ArtistEntryGridModel, favorite: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(favorite = favorite))
    }

    @AssistedFactory
    interface Factory {
        fun create(
            route: AlleyDestination.ArtistMap,
            savedStateHandle: SavedStateHandle,
        ): ArtistMapViewModel
    }
}
