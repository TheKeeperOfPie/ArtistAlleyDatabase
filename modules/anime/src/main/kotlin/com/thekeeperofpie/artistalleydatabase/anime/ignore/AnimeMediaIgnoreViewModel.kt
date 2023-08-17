package com.thekeeperofpie.artistalleydatabase.anime.ignore

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.MediaByIdsQuery
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaGenresController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaLicensorsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.utils.filterOnIO
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
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
    mediaGenresController: MediaGenresController,
    mediaLicensorsController: MediaLicensorsController,
    featureOverrideProvider: FeatureOverrideProvider,
) : ViewModel() {

    val viewer = aniListApi.authedUser
    var query by mutableStateOf("")
    var content = MutableStateFlow(PagingData.empty<MediaEntry>())

    private var initialized = false
    private lateinit var mediaType: MediaType

    val sortFilterController = AnimeSortFilterController(
        sortTypeEnumClass = MediaIgnoreSortOption::class,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        mediaTagsController = mediaTagsController,
        mediaGenresController = mediaGenresController,
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
                .map { it.mapOnIO { MediaEntry(it) } }
                .cachedIn(viewModelScope)
                .applyMediaStatusChanges(
                    statusController = statusController,
                    ignoreList = ignoreList,
                    settings = settings,
                    media = { it.media },
                    forceShowIgnored = true,
                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                        copy(
                            media = media,
                            mediaListStatus = mediaListStatus,
                            progress = progress,
                            progressVolumes = progressVolumes,
                            scoreRaw = scoreRaw,
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
                            .filterOnIO {
                                MediaUtils.filterEntries(
                                    filterParams = filterParams,
                                    entries = listOf(it),
                                    media = { it.media },
                                    forceShowIgnored = true,
                                ).isNotEmpty()
                            }
                            .filterOnIO {
                                val listStatus = it.mediaListStatus
                                if (excludes.isNotEmpty() && excludes.contains(listStatus)) {
                                    return@filterOnIO false
                                }

                                includes.isEmpty() || includes.contains(listStatus)
                            }
                            .filterOnIO {
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

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry) =
        ignoreList.toggle(entry.media.id.toString())

    data class MediaEntry(
        override val media: MediaByIdsQuery.Data.Page.Medium,
        override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
        override val progress: Int? = media.mediaListEntry?.progress,
        override val progressVolumes: Int? = media.mediaListEntry?.progressVolumes,
        override val scoreRaw: Double? = media.mediaListEntry?.score,
        override val ignored: Boolean = false,
        override val showLessImportantTags: Boolean = false,
        override val showSpoilerTags: Boolean = false,
    ) : AnimeMediaListRow.Entry, AnimeMediaCompactListRow.Entry {
        override val color = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)
        override val tags = MediaUtils.buildTags(media, showLessImportantTags, showSpoilerTags)
    }
}
