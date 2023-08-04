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
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaLicensorsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
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
class AnimeMediaIgnoreViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    private val ignoreList: AnimeMediaIgnoreList,
    private val statusController: MediaListStatusController,
    mediaTagsController: MediaTagsController,
    mediaLicensorsController: MediaLicensorsController,
    featureOverrideProvider: FeatureOverrideProvider,
) : ViewModel() {

    val viewer = aniListApi.authedUser
    var query by mutableStateOf("")
    var content = MutableStateFlow(PagingData.empty<AnimeMediaListRow.Entry<Medium>>())
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    private var initialized = false
    private lateinit var mediaType: MediaType

    val sortFilterController = AnimeSortFilterController(
        sortTypeEnumClass = MediaIgnoreSortOption::class,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        mediaTagsController = mediaTagsController,
        mediaLicensorsController = mediaLicensorsController,
    )

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun initialize(mediaType: MediaType) {
        if (initialized) return
        initialized = true
        this.mediaType = mediaType
        sortFilterController.initialize(
            viewModel = this,
            refreshUptimeMillis = refreshUptimeMillis,
            initialParams = AnimeSortFilterController.InitialParams(
                defaultSort = MediaIgnoreSortOption.ID,
                showIgnoredEnabled = false,
                lockSort = false,
            ),
        )

        viewModelScope.launch(CustomDispatchers.Main) {
            @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
            combine(
                refreshUptimeMillis,
                settings.ignoredAniListMediaIds,
                sortFilterController.filterParams(),
                AnimeMediaIgnorePagingSource::RefreshParams,
            )
                .debounce(100.milliseconds)
                .flatMapLatest {
                    Pager(PagingConfig(pageSize = 10, enablePlaceholders = true)) {
                        AnimeMediaIgnorePagingSource(aniListApi, it)
                    }.flow
                }
                .map { it.map { AnimeMediaListRow.Entry(it) } }
                .cachedIn(viewModelScope)
                .applyMediaStatusChanges(
                    statusController = statusController,
                    ignoreList = ignoreList,
                    settings = settings,
                    media = { it.media },
                    forceShowIgnored = true,
                    copy = { mediaListStatus, progress, progressVolumes, ignored, showLessImportantTags, showSpoilerTags ->
                        AnimeMediaListRow.Entry(
                            media = media,
                            mediaListStatus = mediaListStatus,
                            progress = progress,
                            progressVolumes = progressVolumes,
                            ignored = ignored,
                            showLessImportantTags = showLessImportantTags,
                            showSpoilerTags = showSpoilerTags,
                        )
                    },
                )
                .flowOn(CustomDispatchers.IO)
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    combine(
                        snapshotFlow { query }.debounce(500.milliseconds)
                            .flowOn(CustomDispatchers.Main),
                        sortFilterController.filterParams(),
                        ::Pair,
                    ).map { paramsPair ->
                        val (query, filterParams) = paramsPair
                        val includes = filterParams.listStatuses
                            .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                            .map { it.value }
                        val excludes = filterParams.listStatuses
                            .filter { it.state == FilterIncludeExcludeState.EXCLUDE }
                            .map { it.value }
                        pagingData
                            .filter {
                                MediaUtils.filterEntries(
                                    filterParams = filterParams,
                                    entries = listOf(it),
                                    media = { it.media },
                                    forceShowIgnored = true,
                                ).isNotEmpty()
                            }
                            .filter {
                                val listStatus = it.mediaListStatus
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
                }
                .cachedIn(viewModelScope)
                .flowOn(CustomDispatchers.IO)
                .collectLatest(content::emit)
        }
    }

    fun onRefresh() = refreshUptimeMillis.update { SystemClock.uptimeMillis() }

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry<*>) =
        ignoreList.toggle(entry.media.id.toString())
}
