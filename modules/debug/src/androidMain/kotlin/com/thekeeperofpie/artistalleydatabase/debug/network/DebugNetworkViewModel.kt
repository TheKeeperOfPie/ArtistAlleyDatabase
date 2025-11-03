package com.thekeeperofpie.artistalleydatabase.debug.network

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.Inject

@Inject
class DebugNetworkViewModel(
    private val debugNetworkController: DebugNetworkController,
): ViewModel() {

    val graphQlData get() = debugNetworkController.graphQlData

    fun clear() = debugNetworkController.clear()
}
