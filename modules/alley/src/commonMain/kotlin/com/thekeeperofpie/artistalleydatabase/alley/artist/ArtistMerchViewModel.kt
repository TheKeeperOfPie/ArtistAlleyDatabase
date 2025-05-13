package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ArtistMerchViewModel(
    navigationTypeMap: NavigationTypeMap,
    merchEntryDao: MerchEntryDao,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Serializable
    data class InternalRoute(
        val merch: String? = null,
    )

    val route = savedStateHandle.toDestination<InternalRoute>(navigationTypeMap)

    val merchEntry = if (route.merch == null) {
        ReadOnlyStateFlow(null)
    } else {
        flowFromSuspend { merchEntryDao.getMerchById(route.merch) }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)
    }
}
