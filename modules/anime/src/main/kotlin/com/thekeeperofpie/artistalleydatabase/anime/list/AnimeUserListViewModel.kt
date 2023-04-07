package com.thekeeperofpie.artistalleydatabase.anime.list

import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.AuthedUserQuery
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaFilterController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AnimeUserListViewModel @Inject constructor(aniListApi: AuthedAniListApi) : ViewModel() {

    var content by mutableStateOf<ContentState>(ContentState.LoadingEmpty)

    private val filterController =
        AnimeMediaFilterController(MediaListSortOption::class, aniListApi)

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    init {
        filterController.initialize(this, refreshUptimeMillis)
        viewModelScope.launch(CustomDispatchers.Main) {
            @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
            combine(
                aniListApi.authedUser.filterNotNull(),
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
                                    aniListApi.userMediaList(
                                        userId = it.authedUser.id,
                                        sort = it.sortApiValue()
                                    )?.lists
                                        ?.filterNotNull()
                                        ?.map {
                                            mutableListOf<AnimeUserListScreen.Entry>().apply {
                                                this += AnimeUserListScreen.Entry.Header(
                                                    it.name.orEmpty(),
                                                    it.status
                                                )
                                                this += it.entries
                                                    ?.mapNotNull { it?.media }
                                                    ?.map(AnimeUserListScreen.Entry::Item)
                                                    .orEmpty()
                                            }
                                        }
                                        ?.flatten()
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

    private data class RefreshParams(
        val authedUser: AuthedUserQuery.Data.Viewer,
        val requestMillis: Long = SystemClock.uptimeMillis(),
        val sort: MediaListSortOption,
        val sortAscending: Boolean,
    ) {
        fun sortApiValue() = if (sort == MediaListSortOption.DEFAULT) {
            emptyArray()
        } else {
            arrayOf(sort.toApiValue(sortAscending)!!)
        }
    }

    sealed interface ContentState {
        object LoadingEmpty : ContentState

        data class Success(
            val entries: List<AnimeUserListScreen.Entry>,
            val loading: Boolean = false,
        ) : ContentState

        data class Error(
            @StringRes val errorRes: Int? = null,
            val exception: Exception? = null
        ) : ContentState
    }
}