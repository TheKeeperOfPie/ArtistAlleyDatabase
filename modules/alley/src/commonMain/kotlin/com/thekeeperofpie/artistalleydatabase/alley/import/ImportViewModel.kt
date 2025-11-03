package com.thekeeperofpie.artistalleydatabase.alley.import

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.database.AlleyExporter
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.io.writeString

@AssistedInject
class ImportViewModel(
    private val dispatchers: CustomDispatchers,
    private val exporter: AlleyExporter,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val route = savedStateHandle.toDestination<Destinations.Import>(navigationTypeMap)
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
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): ImportViewModel
    }
}
