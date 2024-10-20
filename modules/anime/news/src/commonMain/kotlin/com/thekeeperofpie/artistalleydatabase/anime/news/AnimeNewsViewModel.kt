@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject

@Inject
class AnimeNewsViewModel(
    settings: NewsSettings,
    private val animeNewsController: AnimeNewsController,
) : ViewModel() {

    val sortFilterController = NewsSortFilterController(viewModelScope, settings)

    private val refresh = RefreshFlow()

    var news = combine(refresh.updates, sortFilterController.filterParams, ::Pair)
        .flowOn(CustomDispatchers.Main)
        .flatMapLatest { (_, filterParams) ->
            // More efficient to pre-calculate
            @Suppress("MoveVariableDeclarationIntoWhen")
            val sortOption =
                filterParams.sort.selectedOption(AnimeNewsSortOption.DATETIME)
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
}
