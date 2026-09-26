package com.thekeeperofpie.artistalleydatabase.alley.import

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleyExporter
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.io.writeString

@AssistedInject
class ImportViewModel(
    private val dispatchers: CustomDispatchers,
    private val exporter: AlleyExporter,
    @Assisted private val route: AlleyDestination.Import,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var state by mutableStateOf<LoadingResult<*>>(LoadingResult.empty<Unit>())

    fun confirm() {
        if (state.loading) return
        state = LoadingResult.loading<Unit>()
        viewModelScope.launch(dispatchers.io) {
            Buffer().use {
                it.writeString(route.data)
                state = exporter.import(it)
            }
        }
    }


    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            route: AlleyDestination.Import,
            savedStateHandle: SavedStateHandle,
        ): ImportViewModel
    }
}
