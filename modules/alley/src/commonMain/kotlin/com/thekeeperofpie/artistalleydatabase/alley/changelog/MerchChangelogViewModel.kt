package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.datetime.LocalDate

@AssistedInject
class MerchChangelogViewModel(
    merchEntryDao: MerchEntryDao,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val changes = flowFromSuspend {
        merchEntryDao.getMerchChangelog()
            .sortedByDescending { it.date }
            .map {
                MerchChangelogScreen.DayChange(
                    date = LocalDate.parse(it.date),
                    merchIds = it.merchIds?.sorted().orEmpty(),
                )
            }
    }.stateInForCompose(emptyList())


    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            savedStateHandle: SavedStateHandle,
        ): MerchChangelogViewModel
    }
}
