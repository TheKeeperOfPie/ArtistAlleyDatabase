@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.paging.PagingData
import com.anilist.fragment.MediaDetailsListEntry
import com.anilist.fragment.MediaWithListStatus
import com.anilist.type.MediaListStatus
import com.hoc081098.flowext.combine
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.runningFold

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

suspend fun applyMediaFiltering(
    statuses: Map<String, MediaListStatusController.Update>,
    ignoreController: IgnoreController,
    showAdult: Boolean,
    showIgnored: Boolean,
    showLessImportantTags: Boolean,
    showSpoilerTags: Boolean,
    entry: MediaCompactWithTagsEntry,
) = applyMediaFiltering(
    statuses = statuses,
    ignoreController = ignoreController,
    showAdult = showAdult,
    showIgnored = showIgnored,
    showLessImportantTags = showLessImportantTags,
    showSpoilerTags = showSpoilerTags,
    entry = entry,
    transform = { it },
    media = entry.media,
    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
        copy(
            mediaListStatus = mediaListStatus,
            progress = progress,
            progressVolumes = progressVolumes,
            scoreRaw = scoreRaw,
            ignored = ignored,
            showLessImportantTags = showLessImportantTags,
            showSpoilerTags = showSpoilerTags,
        )
    }
)

suspend fun applyMediaFiltering(
    statuses: Map<String, MediaListStatusController.Update>,
    ignoreController: IgnoreController,
    showAdult: Boolean,
    showIgnored: Boolean,
    showLessImportantTags: Boolean,
    showSpoilerTags: Boolean,
    entry: MediaPreviewEntry,
) = applyMediaFiltering(
    statuses = statuses,
    ignoreController = ignoreController,
    showAdult = showAdult,
    showIgnored = showIgnored,
    showLessImportantTags = showLessImportantTags,
    showSpoilerTags = showSpoilerTags,
    entry = entry,
    transform = { it },
    media = entry.media,
    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
        copy(
            mediaListStatus = mediaListStatus,
            progress = progress,
            progressVolumes = progressVolumes,
            scoreRaw = scoreRaw,
            ignored = ignored,
            showLessImportantTags = showLessImportantTags,
            showSpoilerTags = showSpoilerTags,
        )
    }
)

suspend fun <Input> applyMediaFiltering(
    statuses: Map<String, MediaListStatusController.Update>,
    ignoreController: IgnoreController,
    showAdult: Boolean,
    showIgnored: Boolean,
    showLessImportantTags: Boolean,
    showSpoilerTags: Boolean,
    entry: Input,
    transform: (Input) -> MediaStatusAware,
    media: MediaWithListStatus?,
    forceShowIgnored: Boolean = false,
    copy: Input.(MediaListStatus?, progress: Int?, progressVolumes: Int?, scoreRaw: Double?, ignored: Boolean, showLessImportantTags: Boolean, showSpoilerTags: Boolean) -> Input,
): Input? {
    if (!showAdult && media?.isAdult != false) return null
    val mediaId = media?.id
    val status: MediaListStatus?
    val progress: Int?
    val progressVolumes: Int?
    val scoreRaw: Double?
    if (mediaId == null || !statuses.containsKey(mediaId.toString())) {
        status = media?.mediaListEntry?.status
        progress = media?.mediaListEntry?.progress
        progressVolumes = media?.mediaListEntry?.progressVolumes
        scoreRaw = media?.mediaListEntry?.score
    } else {
        val update = statuses[mediaId.toString()]?.entry
        status = update?.status
        progress = update?.progress
        progressVolumes = update?.progressVolumes
        scoreRaw = update?.score
    }

    val ignored = if (forceShowIgnored) {
        false
    } else {
        mediaId?.toString()?.let { ignoreController.isIgnored(it) } ?: false
    }

    if (!showIgnored && ignored) return null
    val mediaStatusAware = transform(entry)
    return if (status == mediaStatusAware.mediaListStatus
        && mediaStatusAware.progress == progress
        && mediaStatusAware.progressVolumes == progressVolumes
        && mediaStatusAware.scoreRaw == scoreRaw
        && mediaStatusAware.ignored == ignored
        && mediaStatusAware.showLessImportantTags == showLessImportantTags
        && mediaStatusAware.showSpoilerTags == showSpoilerTags
    ) {
        entry
    } else {
        entry.copy(
            status,
            progress,
            progressVolumes,
            scoreRaw,
            ignored,
            showLessImportantTags,
            showSpoilerTags,
        )
    }
}

fun Flow<PagingData<MediaPreviewEntry>>.applyMediaStatusChanges(
    statusController: MediaListStatusController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    forceShowIgnored: Boolean = false,
) = applyMediaStatusChanges(
    statusController = statusController,
    ignoreController = ignoreController,
    settings = settings,
    media = { it.media },
    forceShowIgnored = forceShowIgnored,
    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
        copy(
            mediaListStatus = mediaListStatus,
            progress = progress,
            progressVolumes = progressVolumes,
            scoreRaw = scoreRaw,
            ignored = ignored,
            showLessImportantTags = showLessImportantTags,
            showSpoilerTags = showSpoilerTags
        )
    }
)

fun Flow<PagingData<MediaPreviewWithDescriptionEntry>>.applyMediaStatusChanges2(
    statusController: MediaListStatusController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    forceShowIgnored: Boolean = false,
) = applyMediaStatusChanges(
    statusController = statusController,
    ignoreController = ignoreController,
    settings = settings,
    media = { it.media },
    forceShowIgnored = forceShowIgnored,
    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
        copy(
            mediaListStatus = mediaListStatus,
            progress = progress,
            progressVolumes = progressVolumes,
            scoreRaw = scoreRaw,
            ignored = ignored,
            showLessImportantTags = showLessImportantTags,
            showSpoilerTags = showSpoilerTags
        )
    }
)

fun <T : MediaStatusAware> Flow<PagingData<T>>.applyMediaStatusChanges(
    statusController: MediaListStatusController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    media: (T) -> MediaWithListStatus?,
    forceShowIgnored: Boolean = false,
    copy: T.(MediaListStatus?, progress: Int?, progressVolumes: Int?, scoreRaw: Double?, ignored: Boolean, showLessImportantTags: Boolean, showSpoilerTags: Boolean) -> T,
) = flatMapLatest { pagingData ->
    combine(
        statusController.allChanges(),
        ignoreController.updates(),
        settings.showIgnored,
        settings.showAdult,
        settings.showLessImportantTags,
        settings.showSpoilerTags,
        ::MediaStatusParams,
    ).mapLatest {
        val (statuses, _, showIgnored, showAdult, showLessImportantTags, showSpoilerTags) = it
        pagingData.mapNotNull {
            val mediaPreview = media(it)
            applyMediaFiltering(
                statuses = statuses,
                ignoreController = ignoreController,
                showAdult = showAdult,
                showIgnored = showIgnored,
                showLessImportantTags = showLessImportantTags,
                showSpoilerTags = showSpoilerTags,
                entry = it,
                transform = { it },
                media = mediaPreview,
                forceShowIgnored = forceShowIgnored,
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
