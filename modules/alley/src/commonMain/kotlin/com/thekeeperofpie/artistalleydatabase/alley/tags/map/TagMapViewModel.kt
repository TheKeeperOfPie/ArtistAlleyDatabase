package com.thekeeperofpie.artistalleydatabase.alley.tags.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.transform.transform
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey

@AssistedInject
class TagMapViewModel(
    private val tagEntryDao: TagEntryDao,
    settings: ArtistAlleySettings,
    @Assisted year: DataYear?,
    @Assisted series: String?,
    @Assisted merch: String?,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val booths by transform(viewModelScope) {
        val dataYear = settings.dataYear
        val year = year ?: dataYear
        val showOnlyConfirmedTags = settings.showOnlyConfirmedTags
        produceState(emptySet(), year, showOnlyConfirmedTags) {
            value = tagEntryDao.getBooths(
                year = year,
                TagMapQuery(
                    series = series,
                    merch = merch,
                    showOnlyConfirmedTags = showOnlyConfirmedTags,
                )
            )
        }.value
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            year: DataYear?,
            series: String?,
            merch: String?,
            savedStateHandle: SavedStateHandle,
        ): TagMapViewModel
    }
}
