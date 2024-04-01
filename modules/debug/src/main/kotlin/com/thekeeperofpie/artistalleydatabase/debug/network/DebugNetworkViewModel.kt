package com.thekeeperofpie.artistalleydatabase.debug.network

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DebugNetworkViewModel @Inject constructor(
    private val debugNetworkController: DebugNetworkController,
): ViewModel() {

    val graphQlData get() = debugNetworkController.graphQlData

    fun clear() = debugNetworkController.clear()
}
