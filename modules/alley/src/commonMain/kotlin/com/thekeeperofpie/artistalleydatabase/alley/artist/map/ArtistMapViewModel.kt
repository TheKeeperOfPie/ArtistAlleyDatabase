package com.thekeeperofpie.artistalleydatabase.alley.artist.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ArtistMapViewModel(
    artistEntryDao: ArtistEntryDao,
    seriesEntryCache: SeriesEntryCache,
    userEntryDao: UserEntryDao,
    navigationTypeMap: NavigationTypeMap,
    settings: ArtistAlleySettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toDestination<Destinations.ArtistMap>(navigationTypeMap)
    val id = route.id

    val artist = combine(settings.showOnlyConfirmedTags, settings.showOutdatedCatalogs, ::Pair)
        .flatMapLatest { (showOnlyConfirmedTags, showOutdatedCatalogs) ->
            // Need to observe updates since it's possible to
            // toggle favorite from inside the map
            artistEntryDao.getEntryFlow(id)
                .map {
                    val (series, hasMoreSeries) = ArtistEntryGridModel.getSeriesAndHasMore(
                        randomSeed = randomSeed,
                        showOnlyConfirmedTags = showOnlyConfirmedTags,
                        entry = it,
                        seriesEntryCache = seriesEntryCache,
                    )
                    ArtistEntryGridModel.buildFromEntry(
                        randomSeed = randomSeed,
                        showOnlyConfirmedTags = showOnlyConfirmedTags,
                        entry = it,
                        series = series,
                        hasMoreSeries = hasMoreSeries,
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
}
