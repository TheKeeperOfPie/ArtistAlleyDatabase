package com.thekeeperofpie.artistalleydatabase.anime.activities

import com.anilist.data.fragment.MediaWithListStatus
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaFilterableData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toMediaListStatus
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.runningFold
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class ActivityStatusController {

    private val updates = MutableSharedFlow<Update>(replay = 0, extraBufferCapacity = 5)

    fun onUpdate(update: Update) = updates.tryEmit(update)

    fun allChanges() = updates.runningFold(emptyMap<String, Update>()) { acc, value ->
        acc + (value.activityId to value)
    }

    fun allChanges(filterIds: Set<String>) =
        updates.runningFold(emptyMap<String, Update>()) { acc, value ->
            if (filterIds.contains(value.activityId)) {
                acc + (value.activityId to value)
            } else {
                acc
            }
        }

    fun allChanges(filterActivityId: String) = updates
        .filter { it.activityId == filterActivityId }
        .startWith(null)

    data class Update(
        val activityId: String,
        val liked: Boolean?,
        val subscribed: Boolean?,
        val pending: Boolean = false,
        val error: Throwable? = null,
    )
}

suspend fun <ActivityEntry> applyActivityFiltering(
    mediaListStatuses: Map<String, MediaListStatusController.Update>,
    activityStatuses: Map<String, ActivityStatusController.Update>,
    ignoreController: IgnoreController,
    filteringData: MediaFilteringData,
    entry: ActivityEntry,
    activityId: String?,
    activityStatusAware: ActivityStatusAware?,
    media: MediaWithListStatus?,
    mediaFilterable: MediaFilterableData?,
    copyMedia: ActivityEntry.(MediaFilterableData) -> ActivityEntry,
    copyActivity: ActivityEntry.(liked: Boolean, subscribed: Boolean) -> ActivityEntry,
): ActivityEntry? {
    if (!filteringData.showAdult && media?.isAdult == true) return null
    var copiedEntry = entry
    if (media != null && mediaFilterable != null) {
        val ignored = ignoreController.isIgnored(media.id.toString())
        if (!filteringData.showIgnored && ignored) return null

        val status: MediaListStatus?
        val progress: Int?
        val progressVolumes: Int?
        val mediaId = media.id.toString()
        if (mediaListStatuses.containsKey(mediaId)) {
            val mediaUpdate = mediaListStatuses[mediaId]?.entry
            status = mediaUpdate?.status?.toMediaListStatus()
            progress = mediaUpdate?.progress
            progressVolumes = mediaUpdate?.progressVolumes
        } else {
            status = media.mediaListEntry?.status?.toMediaListStatus()
            progress = media.mediaListEntry?.progress
            progressVolumes = media.mediaListEntry?.progressVolumes
        }
        if (mediaFilterable.mediaListStatus != status
            || mediaFilterable.ignored != ignored
            || mediaFilterable.progress != progress
            || mediaFilterable.progressVolumes != progressVolumes
            || mediaFilterable.showLessImportantTags != filteringData.showLessImportantTags
            || mediaFilterable.showSpoilerTags != filteringData.showSpoilerTags
        ) {
            copiedEntry = copiedEntry.copyMedia(
                mediaFilterable.copy(
                    mediaListStatus = status,
                    progress = progress,
                    progressVolumes = progressVolumes,
                    ignored = ignored,
                    showLessImportantTags = filteringData.showLessImportantTags,
                    showSpoilerTags = filteringData.showSpoilerTags
                )
            )
        }
    }

    if (activityId != null && activityStatusAware != null) {
        val activityUpdate = activityStatuses[activityId]

        val activityLiked = activityStatusAware.liked
        val currentlyLiked = activityUpdate?.liked ?: activityLiked

        val activitySubscribed = activityStatusAware.subscribed
        val currentlySubscribed = activityUpdate?.subscribed ?: activitySubscribed

        if (activityLiked != currentlyLiked || activitySubscribed != currentlySubscribed) {
            copiedEntry = copiedEntry.copyActivity(currentlyLiked, currentlySubscribed)
        }
    }

    return copiedEntry
}
