package com.thekeeperofpie.artistalleydatabase.anime.search

import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.type.MediaSort
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AnimeSearchViewModel @Inject constructor(aniListApi: AuthedAniListApi) : ViewModel() {

    val query = MutableStateFlow("")
    var content by mutableStateOf<ContentState>(ContentState.LoadingEmpty)

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
                ::RefreshParams
            )
                .debounce(100.milliseconds)
                .flatMapLatest {
                    withContext(CustomDispatchers.IO) {
                        flow {
                            emit(ContentState.LoadingEmpty)
                            try {
                                emit(
                                    aniListApi.searchMedia(
                                        query = it.query,
                                        sort = it.sortApiValue()
                                    )
                                        ?.map(AnimeSearchScreen.Entry::Item)
                                        ?.let(ContentState::Success)
                                        ?: ContentState.Error()
                                )
                            } catch (e: Exception) {
                                emit(ContentState.Error(exception = e))
                            }
                        }
                    }
                }
                .collectLatest { content = it }
        }
    }

    fun filterData() = filterController.data()

    fun onRefresh() = refreshUptimeMillis.update { SystemClock.uptimeMillis() }
    fun onQuery(query: String) = this.query.update { query }

    private data class RefreshParams(
        val query: String,
        val requestMillis: Long,
        val sort: MediaSortOption,
        val sortAscending: Boolean,
    ) {
        fun sortApiValue() = if (sort == MediaSortOption.DEFAULT) {
            arrayOf(MediaSort.SEARCH_MATCH)
        } else {
            arrayOf(sort.toApiValue(sortAscending))
        }
    }

    sealed interface ContentState {
        object LoadingEmpty : ContentState

        data class Success(
            val entries: List<AnimeSearchScreen.Entry> = emptyList(),
            val loading: Boolean = false,
        ) : ContentState

        data class Error(
            @StringRes val errorRes: Int? = null,
            val exception: Exception? = null
        ) : ContentState
    }
}