@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.media.data

import androidx.paging.PagingData
import com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

data class MediaFilteringData(
    val showAdult: Boolean,
    val showIgnored: Boolean,
    val showLessImportantTags: Boolean,
    val showSpoilerTags: Boolean,
)

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
    val newStatus: MediaListStatus?
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
    mediaFilteringData: Flow<MediaFilteringData>,
    mediaFilterable: (Input) -> MediaFilterableData?,
    copy: Input.(MediaFilterableData) -> Input,
) = flatMapLatest { pagingData ->
    combine(
        statusController.allChanges(),
        ignoreController.updates(),
        mediaFilteringData,
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

fun MediaDataSettings.mediaFilteringData(forceShowIgnored: Boolean = false) =
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
