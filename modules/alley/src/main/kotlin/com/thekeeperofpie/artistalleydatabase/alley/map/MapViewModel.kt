package com.thekeeperofpie.artistalleydatabase.alley.map

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyUtils
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val application: Application,
    private val artistEntryDao: ArtistEntryDao,
    settings: ArtistAlleySettings,
) : ViewModel() {
    var gridData by mutableStateOf(LoadingResult.loading<GridData>())
    private val mutationUpdates = MutableSharedFlow<ArtistEntry>(5, 5)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            val booths = artistEntryDao.getBooths()
            val letterToBooths = booths.groupBy { it.take(1) }.toSortedMap()
            val maxRow = letterToBooths.size
            val maxColumn =
                letterToBooths.maxOf {
                    it.value.mapNotNull { it.drop(1).toIntOrNull() }.maxOrNull() ?: 0
                }
            var currentIndex = 0
            val showRandomCatalogImage = settings.showRandomCatalogImage.value
            val tables = letterToBooths.values.mapIndexed { letterIndex, booths ->
                booths.map {
                    val tableNumber = it.filter { it.isDigit() }.toInt()
                    Table(
                        booth = it,
                        section = Table.Section.fromTableNumber(tableNumber),
                        image = ArtistAlleyUtils.getImages(application, "catalogs", it).let {
                            if (showRandomCatalogImage) {
                                it.randomOrNull()
                            } else {
                                it.firstOrNull()
                            }
                        },
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
            withContext(CustomDispatchers.Main) {
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
                artistEntryDao.insertEntries(it)
            }
        }
    }

    fun onFavoriteToggle(entry: ArtistEntryGridModel, favorite: Boolean) {
        mutationUpdates.tryEmit(entry.value.copy(favorite = favorite))
    }

    fun onIgnoredToggle(entry: ArtistEntryGridModel, ignored: Boolean) {
        mutationUpdates.tryEmit(entry.value.copy(ignored = ignored))
    }

    suspend fun tableEntry(table: Table) = artistEntryDao.getEntry(table.booth)?.let {
        ArtistEntryGridModel.buildFromEntry(application, it)
    }

    data class GridData(
        val maxX: Int,
        val maxY: Int,
        val tables: List<Table>,
    )
}
