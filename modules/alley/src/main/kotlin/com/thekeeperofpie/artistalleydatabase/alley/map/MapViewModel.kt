package com.thekeeperofpie.artistalleydatabase.alley.map

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            val tables = letterToBooths.values.mapIndexed { index, booths ->
                booths.map {
                    val tableNumber = it.filter { it.isDigit() }.toInt()
                    Table(
                        booth = it,
                        section = Table.Section.fromTableNumber(tableNumber),
                        image = ArtistAlleyUtils.getImages(application, "catalogs", it)
                            .firstOrNull(),
                        gridX = index,
                        // There's a physical gap not accounted for in the numbers between 41 and 42
                        gridY = if (tableNumber >= 42) tableNumber + 1 else tableNumber,
                    )
                }
            }.flatten()
            withContext(CustomDispatchers.Main) {
                gridData = LoadingResult.success(
                    GridData(
                        maxRow = maxRow,
                        maxColumn = maxColumn,
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
        val maxRow: Int,
        val maxColumn: Int,
        val tables: List<Table>,
    )
}
