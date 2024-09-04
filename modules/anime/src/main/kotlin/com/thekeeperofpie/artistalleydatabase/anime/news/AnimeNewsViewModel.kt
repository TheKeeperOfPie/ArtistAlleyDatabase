@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.news.AnimeNewsController
import com.thekeeperofpie.artistalleydatabase.news.AnimeNewsEntry
import com.thekeeperofpie.artistalleydatabase.news.NewsSettings
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class AnimeNewsViewModel @Inject constructor(
    animeSettings: AnimeSettings,
    settings: NewsSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val animeNewsController: AnimeNewsController,
) : ViewModel() {

    val sortFilterController =
        NewsSortFilterController(viewModelScope, animeSettings, settings, featureOverrideProvider)

    var news by mutableStateOf<List<AnimeNewsEntry<*>>?>(null)
        private set

    private val refresh = MutableStateFlow(-1L)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            combine(
                refresh,
                sortFilterController.filterParams,
                ::Pair
            )
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { (_, filterParams) ->
                    animeNewsController.news().mapLatest { news ->
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

                        news?.sortedWith(comparator)
                    }.startWith(null)
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { news = it }
        }
    }

    fun refresh() {
        refresh.value = Clock.System.now().toEpochMilliseconds()
    }
}
