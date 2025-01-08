package com.thekeeperofpie.artistalleydatabase.alley.tags.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class TagMapViewModel(
    private val tagEntryDao: TagEntryDao,
    navigationTypeMap: NavigationTypeMap,
    settings: ArtistAlleySettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Serializable
    data class InternalRoute(
        val series: String? = null,
        val merch: String? = null,
    )

    val route = savedStateHandle.toDestination<InternalRoute>(navigationTypeMap)

    var booths by mutableStateOf(emptySet<String>())
        private set

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            settings.showOnlyConfirmedTags
                .mapLatest {
                    tagEntryDao.getBooths(
                        TagMapQuery(
                            series = route.series,
                            merch = route.merch,
                            showOnlyConfirmedTags = it,
                        )
                    )
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { booths = it }
        }
    }
}