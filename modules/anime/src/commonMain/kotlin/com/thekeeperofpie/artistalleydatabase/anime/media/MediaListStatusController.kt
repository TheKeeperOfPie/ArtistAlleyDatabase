@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.paging.PagingData
import com.anilist.fragment.MediaDetailsListEntry
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.data.MediaFilterableData
import com.thekeeperofpie.artistalleydatabase.anime.data.toMediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.runningFold
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class MediaListStatusController {

    private val updates = MutableSharedFlow<Update>(replay = 0, extraBufferCapacity = 5)

    suspend fun onUpdate(
        mediaId: String,
        entry: MediaDetailsListEntry?,
    ) = updates.emit(Update(mediaId = mediaId, entry = entry))

    fun allChanges() = updates.runningFold(emptyMap<String, Update>()) { acc, value ->
        acc + (value.mediaId to value)
    }

    fun allChanges(filterIds: Set<String>) =
        updates.runningFold(emptyMap<String, Update>()) { acc, value ->
            if (filterIds.contains(value.mediaId)) {
                acc + (value.mediaId to value)
            } else {
                acc
            }
        }

    fun allChanges(filterMediaId: String) = updates
        .filter { it.mediaId == filterMediaId }
        .startWith(null)

    data class Update(
        val mediaId: String,
        val entry: MediaDetailsListEntry? = null,
    )
}

data class MediaFilteringData(
    val showAdult: Boolean,
    val showIgnored: Boolean,
    val showLessImportantTags: Boolean,
    val showSpoilerTags: Boolean,
)

fun AnimeSettings.mediaFilteringData(forceShowIgnored: Boolean = false) =
    combineStates(
        showAdult,
        showIgnored,
        showLessImportantTags,
        showSpoilerTags,
    ) { showAdult, showIgnored, showLessImportantTags, showSpoilerTags ->
        MediaFilteringData(
            showAdult = showAdult,
            showIgnored = if (forceShowIgnored) true else showIgnored,
            showLessImportantTags = showLessImportantTags,
            showSpoilerTags = showSpoilerTags
        )
    }

suspend fun <Input> applyMediaFiltering(
    statuses: Map<String, MediaListStatusController.Update>,
    ignoreController: IgnoreController,
    filteringData: MediaFilteringData,
    entry: Input,
    filterableData: MediaFilterableData,
    copy: Input.(MediaFilterableData) -> Input,
): Input? {
    val (showAdult, showIgnored, showLessImportantTags, showSpoilerTags) = filteringData
    val (mediaId, isAdult, mediaListStatus, progress, progressVolumes, scoreRaw) = filterableData
    if (!showAdult && isAdult != false) return null
    val newStatus: com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus?
    val newProgress: Int?
    val newProgressVolumes: Int?
    val newScoreRaw: Double?
    if (!statuses.containsKey(mediaId.toString())) {
        newStatus = mediaListStatus
        newProgress = progress
        newProgressVolumes = progressVolumes
        newScoreRaw = scoreRaw
    } else {
        val update = statuses[mediaId.toString()]?.entry
        newStatus = update?.status?.toMediaListStatus()
        newProgress = update?.progress
        newProgressVolumes = update?.progressVolumes
        newScoreRaw = update?.score
    }

    val ignored = ignoreController.isIgnored(mediaId)
    if (!showIgnored && ignored) return null
    return if (newStatus == filterableData.mediaListStatus
        && filterableData.progress == newProgress
        && filterableData.progressVolumes == newProgressVolumes
        && filterableData.scoreRaw == newScoreRaw
        && filterableData.ignored == ignored
        && filterableData.showLessImportantTags == showLessImportantTags
        && filterableData.showSpoilerTags == showSpoilerTags
    ) {
        entry
    } else {
        entry.copy(
            filterableData.copy(
                mediaListStatus = newStatus,
                progress = newProgress,
                progressVolumes = newProgressVolumes,
                scoreRaw = newScoreRaw,
                ignored = ignored,
                showLessImportantTags = showLessImportantTags,
                showSpoilerTags = showSpoilerTags,
            )
        )
    }
}

fun <Input : Any> Flow<PagingData<Input>>.applyMediaStatusChanges(
    statusController: MediaListStatusController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    mediaFilterable: (Input) -> MediaFilterableData?,
    copy: Input.(MediaFilterableData) -> Input,
) = flatMapLatest { pagingData ->
    combine(
        statusController.allChanges(),
        ignoreController.updates(),
        settings.mediaFilteringData(),
    ) { statuses, _, filteringData ->
        pagingData.mapNotNull {
            @Suppress("NAME_SHADOWING")
            val mediaFilterable = mediaFilterable(it) ?: return@mapNotNull null
            applyMediaFiltering(
                statuses = statuses,
                ignoreController = ignoreController,
                filteringData = filteringData,
                entry = it,
                filterableData = mediaFilterable,
                copy = copy,
            )
        }
    }
}

private data class MediaStatusParams(
    val statuses: Map<String, MediaListStatusController.Update>,
    val ignoredCount: Int,
    val showIgnored: Boolean,
    val showAdult: Boolean,
    val showLessImportantTags: Boolean,
    val showSpoilerTags: Boolean,
)
