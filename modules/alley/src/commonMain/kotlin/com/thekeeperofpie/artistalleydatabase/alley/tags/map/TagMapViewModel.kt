package com.thekeeperofpie.artistalleydatabase.alley.tags.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Named
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class TagMapViewModel(
    private val tagEntryDao: TagEntryDao,
    settings: ArtistAlleySettings,
    @Assisted year: DataYear?,
    @Assisted @Named("series") series: String?,
    @Assisted @Named("merch") merch: String?,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var booths by mutableStateOf(emptySet<String>())
        private set

    private val dataYear = if (year == null) {
        settings.dataYear
    } else {
        flowOf(year)
    }

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            combine(settings.showOnlyConfirmedTags, dataYear, ::Pair)
                .mapLatest { (showOnlyConfirmedTags, year) ->
                    tagEntryDao.getBooths(
                        year = year,
                        TagMapQuery(
                            series = series,
                            merch = merch,
                            showOnlyConfirmedTags = showOnlyConfirmedTags,
                        )
                    )
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { booths = it }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            year: DataYear?,
            @Named("series") series: String?,
            @Named("merch") merch: String?,
            savedStateHandle: SavedStateHandle,
        ): TagMapViewModel
    }
}
