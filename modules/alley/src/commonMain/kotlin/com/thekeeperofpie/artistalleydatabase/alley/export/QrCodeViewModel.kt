package com.thekeeperofpie.artistalleydatabase.alley.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleyExporter
import com.thekeeperofpie.artistalleydatabase.alley.settings.ImportExportUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.io.readString
import me.tatarka.inject.annotations.Inject

@Inject
class QrCodeViewModel(
    private val dispatchers: CustomDispatchers,
    private val exporter: AlleyExporter,
) : ViewModel() {

    val data = flowFromSuspend {
        Buffer().use {
            exporter.exportPartial(it)
            it.readString()
        }
    }.flowOn(dispatchers.io)
        .stateInForCompose(null)

    fun download() {
        viewModelScope.launch(dispatchers.io) {
            val data = Buffer().use {
                exporter.exportFull(it)
                it.readString()
            }
            ImportExportUtils.download(true, data)
        }
    }
}
