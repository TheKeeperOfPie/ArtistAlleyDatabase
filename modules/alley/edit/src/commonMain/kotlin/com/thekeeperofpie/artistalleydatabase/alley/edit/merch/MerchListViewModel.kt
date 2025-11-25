package com.thekeeperofpie.artistalleydatabase.alley.edit.merch

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.paging.PagingData
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class MerchListViewModel(
    database: AlleyEditDatabase,
    dispatchers: CustomDispatchers,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val query by savedStateHandle.saveable(saver = TextFieldState.Saver) { TextFieldState() }
    private val refresh = RefreshFlow()

    private val allMerch = refresh.updates
        .mapLatest {
            database.loadMerch()
                .values
                .sortedBy { it.name }
        }
        .flowOn(dispatchers.io)

    private val debouncedQuery = snapshotFlow { query.text.toString() }.debounce(1.seconds)
    val merch =
        combine(debouncedQuery, allMerch, ::Pair)
            .mapLatest { (query, merch) ->
                if (query.isEmpty()) return@mapLatest merch
                val (firstSection, firstRemaining) = merch.partition {
                    it.name.contains(query, ignoreCase = true)
                }
                val (secondSection, _) = firstRemaining.partition {
                    it.notes?.contains(query, ignoreCase = true) == true
                }
                firstSection + secondSection
            }
            .mapLatest { PagingData.from(it) }
            .flowOn(dispatchers.io)
            .stateIn(viewModelScope, SharingStarted.Eagerly, PagingData.empty())

    fun refresh() = refresh.refresh()

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): MerchListViewModel
    }
}
