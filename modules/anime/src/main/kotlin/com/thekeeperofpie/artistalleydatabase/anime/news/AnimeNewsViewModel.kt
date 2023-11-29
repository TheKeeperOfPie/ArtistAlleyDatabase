@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.news

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeNewsViewModel @Inject constructor(
    settings: AnimeSettings,
    private val animeNewsController: AnimeNewsController,
    featureOverrideProvider: FeatureOverrideProvider,
) : ViewModel() {

    val sortFilterController = NewsSortFilterController(settings, featureOverrideProvider)

    var news by mutableStateOf<List<AnimeNewsArticleEntry<*>>?>(null)
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
                        val baseComparator: Comparator<AnimeNewsArticleEntry<*>> =
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
        refresh.value = SystemClock.uptimeMillis()
    }
}
