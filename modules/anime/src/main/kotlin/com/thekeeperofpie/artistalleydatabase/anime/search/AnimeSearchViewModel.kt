package com.thekeeperofpie.artistalleydatabase.anime.search

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.anilist.MediaAdvancedSearchQuery.Data.Page.Medium
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaSortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AnimeSearchViewModel @Inject constructor(aniListApi: AuthedAniListApi) : ViewModel() {

    val query = MutableStateFlow("")
    var content = MutableStateFlow(PagingData.empty<AnimeSearchScreen.Entry>())

    private val filterController = AnimeMediaFilterController(MediaSortOption::class)

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
            combine(
                query.debounce(200.milliseconds),
                refreshUptimeMillis,
                filterController.sort,
                filterController.sortAscending,
                AnimeMediaSearchPagingSource::RefreshParams
            )
                .flowOn(CustomDispatchers.IO)
                .debounce(100.milliseconds)
                .flatMapLatest {
                    Pager(PagingConfig(pageSize = 10, enablePlaceholders = true)) {
                        AnimeMediaSearchPagingSource(aniListApi, it)
                    }.flow
                }
                .map {
                    it.map<Medium, AnimeSearchScreen.Entry> { AnimeSearchScreen.Entry.Item(it) }
                }
                .collectLatest(content::emit)
        }
    }

    fun filterData() = filterController.data()

    fun onRefresh() = refreshUptimeMillis.update { SystemClock.uptimeMillis() }
    fun onQuery(query: String) = this.query.update { query }
}