package com.thekeeperofpie.artistalleydatabase.alley.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleyExporter
import com.thekeeperofpie.artistalleydatabase.alley.settings.ImportExportUtils
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.readString

@Inject
class QrCodeViewModel(
    private val dispatchers: CustomDispatchers,
    private val exporter: AlleyExporter,
) : ViewModel() {

    fun download() {
        viewModelScope.launch(dispatchers.io) {
            val data = Buffer().use {
                exporter.exportFull(it)
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
}
