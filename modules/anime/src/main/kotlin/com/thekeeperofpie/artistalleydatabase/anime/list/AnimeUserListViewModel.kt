package com.thekeeperofpie.artistalleydatabase.anime.list

import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AnimeUserListViewModel @Inject constructor(aniListApi: AuthedAniListApi) : ViewModel() {

    var content by mutableStateOf<ContentState>(ContentState.Loading)

    private val refreshUptimeMillis = MutableStateFlow<Long>(-1)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            @OptIn(ExperimentalCoroutinesApi::class)
            combine(aniListApi.authedUser.filterNotNull(), refreshUptimeMillis, ::Pair)
                .flatMapLatest {
                    withContext(CustomDispatchers.IO) {
                        flow {
                            emit(ContentState.Loading)
                            try {
                                emit(
                                    aniListApi.userMediaList()?.lists
                                        ?.filterNotNull()
                                        ?.map {
                                            mutableListOf<MediaListEntry>().apply {
                                                this += MediaListEntry.Header(
                                                    it.name.orEmpty(),
                                                    it.status
                                                )
                                                this += it.entries
                                                    ?.filterNotNull()
                                                    ?.map(MediaListEntry::Item)
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

    fun onRefresh() {
        refreshUptimeMillis.update { SystemClock.uptimeMillis() }
    }

    sealed interface ContentState {
        object Loading : ContentState

        data class Success(val entries: List<MediaListEntry>) : ContentState

        data class Error(
            @StringRes val errorRes: Int? = null,
            val exception: Exception? = null
        ) : ContentState
    }
}