@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import com.anilist.fragment.MediaPreview
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.runningFold

class MediaListStatusController {

    val updates =
        MutableSharedFlow<Pair<String, MediaListStatus?>>(replay = 5, extraBufferCapacity = 5)
            .apply { tryEmit("" to null) }

    suspend fun onUpdate(mediaId: String, newStatus: MediaListStatus?) =
        updates.emit(mediaId to newStatus)

    fun allChanges() =
        updates.runningFold(emptyMap<String, MediaListStatus?>()) { acc, value -> acc + value }

    fun allChanges(filterIds: Set<String>) =
        updates.runningFold(emptyMap<String, MediaListStatus?>()) { acc, value ->
            if (filterIds.contains(value.first)) {
                acc + value
            } else {
                acc
            }
        }
}

fun <Input> applyStatusAndIgnored(
    statuses: Map<String, MediaListStatus?>,
    ignoredIds: Set<Int>,
    entry: Input,
    transform: (Input) -> MediaStatusAware,
    media: MediaPreview?,
    copy: Input.(MediaListStatus?, ignored: Boolean) -> Input,
): Input {
    val mediaId = media?.id
    val status = if (mediaId == null || !statuses.containsKey(mediaId.toString())) {
        media?.mediaListEntry?.status
    } else {
        statuses[mediaId.toString()]
    }

    val ignored = ignoredIds.contains(mediaId)
    val mediaStatusAware = transform(entry)
    return if (status == mediaStatusAware.mediaListStatus && mediaStatusAware.ignored == ignored) {
        entry
    } else {
        entry.copy(status, ignored)
    }
}

fun <T : MediaStatusAware> Flow<PagingData<T>>.applyMediaStatusChanges(
    statusController: MediaListStatusController,
    ignoreList: AnimeMediaIgnoreList,
    settings: AnimeSettings,
    media: (T) -> MediaPreview?,
    forceShowIgnored: Boolean = false,
    copy: T.(MediaListStatus?, ignored: Boolean) -> T,
) = flatMapLatest { pagingData ->
    combine(
        statusController.allChanges(),
        if (forceShowIgnored) flowOf(emptySet()) else ignoreList.updates,
        settings.showIgnored,
        settings.showAdult,
        ::MediaStatusParams,
    ).mapLatest {
        val (statuses, ignoredIds, showIgnored, showAdult) = it
        pagingData
            .filter { showAdult || (media(it)?.isAdult == false) }
            .map {
                val mediaPreview = media(it)
                applyStatusAndIgnored(statuses, ignoredIds, it, { it }, mediaPreview, copy)
            }
            .filter { showIgnored || !it.ignored }
    }
}

private data class MediaStatusParams(
    val statuses: Map<String, MediaListStatus?>,
    val ignoredIds: Set<Int>,
    val showIgnored: Boolean,
    val showAdult: Boolean,
)
