package com.thekeeperofpie.artistalleydatabase.debug.network

import androidx.lifecycle.ViewModel
import me.tatarka.inject.annotations.Inject

@Inject
class DebugNetworkViewModel(
    private val debugNetworkController: DebugNetworkController,
): ViewModel() {

    val graphQlData get() = debugNetworkController.graphQlData

    fun clear() = debugNetworkController.clear()
}
