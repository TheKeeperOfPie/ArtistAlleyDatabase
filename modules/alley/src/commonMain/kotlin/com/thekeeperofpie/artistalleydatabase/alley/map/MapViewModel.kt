package com.thekeeperofpie.artistalleydatabase.alley.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.BoothWithFavorite
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.math.absoluteValue
import kotlin.random.Random

@Inject
class MapViewModel(
    private val artistEntryDao: ArtistEntryDao,
    private val seriesEntryDao: SeriesEntryDao,
    private val userEntryDao: UserEntryDao,
    private val settings: ArtistAlleySettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    var gridData by mutableStateOf(LoadingResult.loading<GridData>())
    private val randomSeed =
        savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }
    private val mutationUpdates = MutableSharedFlow<ArtistUserEntry>(5, 5)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            // TODO: This is very inefficient to respond to favorites updates
            userEntryDao.getBoothsWithFavorites()
                .map(::mapBooths)
                .flowOn(CustomDispatchers.IO)
                .collectLatest { tables ->
                    gridData = LoadingResult.success(
                        GridData(
                            maxX = tables.maxOfOrNull { it.gridX } ?: 0,
                            maxY = tables.maxOfOrNull { it.gridY } ?: 0,
                            tables = tables,
                        )
                    )
                }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            mutationUpdates.collectLatest {
                userEntryDao.insertArtistUserEntry(it)
            }
        }
    }

    private fun mapBooths(booths: List<BoothWithFavorite>): List<Table> {
        @Suppress("UNCHECKED_CAST")
        val letterToBooths = (booths.groupBy { it.booth?.take(1) }
            .filterKeys { it != null } as Map<String, List<BoothWithFavorite>>)
            .toList()
            .sortedBy { it.first }
        var currentIndex = 0
        val showRandomCatalogImage = settings.showRandomCatalogImage.value
        return letterToBooths.mapIndexed { letterIndex, pair ->
            pair.second.mapNotNull {
                val booth = it.booth ?: return@mapNotNull null
                val tableNumber = it.booth.filter { it.isDigit() }.toInt()
                val images = AlleyDataUtils.getArtistImages(
                    year = it.year,
                    booth = it.booth,
                    name = it.name,
                )
                val imageIndex = if (showRandomCatalogImage) {
                    images.indices.randomOrNull()
                } else {
                    0
                }
                Table(
                    year = it.year,
                    id = it.id,
                    booth = it.booth,
                    section = Table.Section.fromTableNumber(tableNumber),
                    image = imageIndex?.let(images::getOrNull),
                    imageIndex = imageIndex,
                    favorite = it.favorite,
                    gridX = currentIndex,
                    // There's a physical gap not accounted for in the numbers between 41 and 42
                    gridY = if (tableNumber >= 42) tableNumber + 1 else tableNumber,
                )
            }.also {
                currentIndex++
                if (letterIndex == 11) {
                    // There's a large gap between the two halves of the map
                    currentIndex += 4
                } else if (letterIndex % 2 == 0) {
                    // Skip an extra between every 2 tables
                    currentIndex++
                }
            }
        }.flatten()
    }

    fun onFavoriteToggle(entry: ArtistEntryGridModel, favorite: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(favorite = favorite))
    }

    fun onIgnoredToggle(entry: ArtistEntryGridModel, ignored: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(ignored = ignored))
    }

    suspend fun tableEntry(table: Table) = artistEntryDao.getEntry(table.year, table.id)?.let {
        val series = ArtistEntryGridModel.getSeries(
            showOnlyConfirmedTags = false,
            entry = it,
            seriesEntryDao = seriesEntryDao,
        )
        ArtistEntryGridModel.buildFromEntry(
            randomSeed = randomSeed,
            showOnlyConfirmedTags = false, // This shouldn't matter here
            entry = it,
            series = series,
        )
    }

    data class GridData(
        val maxX: Int,
        val maxY: Int,
        val tables: List<Table>,
    )
}
