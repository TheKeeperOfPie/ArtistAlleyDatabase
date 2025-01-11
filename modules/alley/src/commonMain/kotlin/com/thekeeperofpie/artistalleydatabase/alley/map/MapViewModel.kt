package com.thekeeperofpie.artistalleydatabase.alley.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyUtils
import com.thekeeperofpie.artistalleydatabase.alley.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.GetBoothsWithFavorites
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class MapViewModel(
    private val artistEntryDao: ArtistEntryDao,
    private val userEntryDao: UserEntryDao,
    private val settings: ArtistAlleySettings,
) : ViewModel() {
    var gridData by mutableStateOf(LoadingResult.loading<GridData>())
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
                            maxX = tables.maxOf { it.gridX },
                            maxY = tables.maxOf { it.gridY },
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

    private fun mapBooths(booths: List<GetBoothsWithFavorites>): List<Table> {
        val letterToBooths = booths.groupBy { it.booth.take(1) }.toList().sortedBy { it.first }
        var currentIndex = 0
        val showRandomCatalogImage = settings.showRandomCatalogImage.value
        return letterToBooths.mapIndexed { letterIndex, pair ->
            pair.second.map {
                val tableNumber = it.booth.filter { it.isDigit() }.toInt()
                val images = ArtistAlleyUtils.getImages(
                    folder = ArtistAlleyUtils.Folder.CATALOGS,
                    file = it.booth,
                )
                val imageIndex = if (showRandomCatalogImage) {
                    images.indices.randomOrNull()
                } else {
                    0
                }
                Table(
                    booth = it.booth,
                    section = Table.Section.fromTableNumber(tableNumber),
                    image = imageIndex?.let(images::getOrNull),
                    imageIndex = imageIndex,
                    favorite = it.favorite == true,
                    gridX = currentIndex,
                    // There's a physical gap not accounted for in the numbers between 41 and 42
                    gridY = if (tableNumber >= 42) tableNumber + 1 else tableNumber,
                )
            }.also {
                currentIndex++
                if (letterIndex % 2 == 0) {
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

    suspend fun tableEntry(table: Table) = artistEntryDao.getEntry(table.booth)?.let {
        ArtistEntryGridModel.buildFromEntry(it)
    }

    data class GridData(
        val maxX: Int,
        val maxY: Int,
        val tables: List<Table>,
    )
}
