package com.thekeeperofpie.artistalleydatabase.anime.ignore

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.anilist.MediaByIdsQuery.Data.Page.Medium
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.utils.IncludeExcludeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AnimeMediaIgnoreViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    private val ignoreList: AnimeMediaIgnoreList,
) : ViewModel() {

    var query by mutableStateOf("")
    var content =
        MutableStateFlow(PagingData.empty<AnimeMediaListRow.MediaEntry<Medium>>())
    var tagShown by mutableStateOf<AnimeMediaFilterController.TagSection.Tag?>(null)
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    private var initialized = false
    private lateinit var mediaType: MediaType

    private val filterController =
        AnimeMediaFilterController(MediaIgnoreSortOption::class, aniListApi, settings, ignoreList)

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun initialize(mediaType: MediaType) {
        if (initialized) return
        initialized = true
        this.mediaType = mediaType
        filterController.initialize(
            this, refreshUptimeMillis, AnimeMediaFilterController.InitialParams(
                isAnime = mediaType == MediaType.ANIME,
                showListStatusExcludes = true,
            )
        )

        viewModelScope.launch(CustomDispatchers.Main) {
            @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
            combine(
                refreshUptimeMillis,
                settings.ignoredAniListMediaIds,
                filterController.sortOptions,
                filterController.sortAscending,
                AnimeMediaIgnorePagingSource::RefreshParams,
            )
                .debounce(100.milliseconds)
                .flatMapLatest {
                    combine(
                        Pager(PagingConfig(pageSize = 10, enablePlaceholders = true)) {
                            AnimeMediaIgnorePagingSource(aniListApi, it)
                        }.flow.cachedIn(viewModelScope),
                        snapshotFlow { query }.debounce(500.milliseconds),
                        filterController.filterParams(),
                        ::Triple
                    )
                }
                .map { (pagingData, query, filterParams) ->
                    val includes = filterParams.listStatuses
                        .filter { it.state == IncludeExcludeState.INCLUDE }
                        .map { it.value }
                    val excludes = filterParams.listStatuses
                        .filter { it.state == IncludeExcludeState.EXCLUDE }
                        .map { it.value }
                    pagingData.map { AnimeMediaListRow.MediaEntry(it, ignored = false) }
                        .filter {
                            filterController.filterEntries(
                                filterParams = filterParams,
                                entries = listOf(it),
                                forceShowIgnored = true,
                            ).isNotEmpty()
                        }
                        .filter {
                            val listStatus = it.media.mediaListEntry?.status
                            if (excludes.isNotEmpty() && excludes.contains(listStatus)) {
                                return@filter false
                            }

                            includes.isEmpty() || includes.contains(listStatus)
                        }
                        .filter {
                            listOfNotNull(
                                it.media.title?.romaji,
                                it.media.title?.english,
                                it.media.title?.native,
                            ).plus(it.media.synonyms?.filterNotNull().orEmpty())
                                .any { it.contains(query, ignoreCase = true) }
                        }
                }
                .collectLatest(content::emit)
        }
    }

    fun filterData() = filterController.data()

    fun onRefresh() = refreshUptimeMillis.update { SystemClock.uptimeMillis() }

    fun onTagLongClick(tagId: String) {
        tagShown = filterController.tagsByCategory.value.values
            .asSequence()
            .mapNotNull { it.findTag(tagId) }
            .firstOrNull()
    }

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry) {
        val mediaId = entry.id?.valueId ?: return
        val ignored = !ignoreList.get(mediaId)
        ignoreList.set(mediaId, ignored)
        entry.ignored = !ignored
    }
}
