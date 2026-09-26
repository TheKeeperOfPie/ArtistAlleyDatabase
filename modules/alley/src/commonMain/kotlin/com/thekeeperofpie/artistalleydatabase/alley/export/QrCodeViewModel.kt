package com.thekeeperofpie.artistalleydatabase.alley.export

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleyExporter
import com.thekeeperofpie.artistalleydatabase.alley.settings.ImportExportUtils
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.readString

@AssistedInject
class QrCodeViewModel(
    private val dispatchers: CustomDispatchers,
    private val exporter: AlleyExporter,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    fun download(includeMetadata: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            val data = Buffer().use {
                exporter.exportFull(includeMetadata, it)
                it.readString()
            }
            ImportExportUtils.download(true, data)
        }
    }

    suspend fun exportPartialForYear(year: DataYear) = withContext(dispatchers.io) {
        Buffer().use {
            exporter.exportPartial(it, year)
            it.readString()
        }
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(savedStateHandle: SavedStateHandle): QrCodeViewModel
    }
}
