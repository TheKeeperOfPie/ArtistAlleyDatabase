package com.thekeeperofpie.artistalleydatabase.alley.tags.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TagMapViewModel @Inject constructor(
    private val tagEntryDao: TagEntryDao,
    savedStateHandle: SavedStateHandle,
    settings: ArtistAlleySettings,
) : ViewModel() {

    @Serializable
    data class InternalRoute(
        val series: String? = null,
        val merch: String? = null,
    )

    val route = savedStateHandle.toRoute<InternalRoute>()

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
