@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@AssistedInject
class AnimeNewsViewModel(
    private val animeNewsController: AnimeNewsController,
    @Assisted newsSortFilterViewModel: NewsSortFilterViewModel,
) : ViewModel() {

    private val refresh = RefreshFlow()

    // TODO: Loading indicator isn't propagated from AnimeNewsController
    var news = combine(refresh.updates, newsSortFilterViewModel.state.filterParams, ::Pair)
        .flatMapLatest { (_, filterParams) ->
            // More efficient to pre-calculate
            @Suppress("MoveVariableDeclarationIntoWhen")
            val sortOption = filterParams.sort
            val baseComparator: Comparator<AnimeNewsEntry<*>> =
                when (sortOption) {
                    AnimeNewsSortOption.DATETIME -> compareBy { it.date }
                    AnimeNewsSortOption.TITLE -> compareBy { it.title }
                    AnimeNewsSortOption.SOURCE -> compareBy { it.type }
                }

            val comparator = nullsFirst(baseComparator).let {
                if (filterParams.sortAscending) it else it.reversed()
            }

            animeNewsController.news
                .mapLatest { news -> news.transformResult { it.sortedWith(comparator) } }
        }
        .flowOn(CustomDispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, LoadingResult.loading())

    fun refresh() = refresh.refresh()

    @AssistedFactory
    interface Factory {
        fun create(newsSortFilterViewModel: NewsSortFilterViewModel): AnimeNewsViewModel
    }
}
