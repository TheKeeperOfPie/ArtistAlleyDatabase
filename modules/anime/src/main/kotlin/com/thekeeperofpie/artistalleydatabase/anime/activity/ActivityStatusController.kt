package com.thekeeperofpie.artistalleydatabase.anime.activity

import com.anilist.fragment.MediaWithListStatus
import com.anilist.type.MediaListStatus
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.runningFold

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

fun <ActivityEntry> applyActivityFiltering(
    mediaListStatuses: Map<String, MediaListStatusController.Update>,
    activityStatuses: Map<String, ActivityStatusController.Update>,
    ignoredIds: Set<Int>,
    showAdult: Boolean,
    showIgnored: Boolean,
    showLessImportantTags: Boolean,
    showSpoilerTags: Boolean,
    entry: ActivityEntry,
    activityId: String?,
    activityStatusAware: ActivityStatusAware?,
    media: MediaWithListStatus?,
    mediaStatusAware: MediaStatusAware?,
    copyMedia: ActivityEntry.(status: MediaListStatus?, progress: Int?, progressVolumes: Int?, ignored: Boolean, showLessImportantTags: Boolean, showSpoilerTags: Boolean) -> ActivityEntry,
    copyActivity: ActivityEntry.(liked: Boolean, subscribed: Boolean) -> ActivityEntry,
): ActivityEntry? {
    if (!showAdult && media?.isAdult == true) return null
    var copiedEntry = entry
    if (media != null && mediaStatusAware != null) {
        val ignored = ignoredIds.contains(media.id)
        if (!showIgnored && ignored) return null

        val status: MediaListStatus?
        val progress: Int?
        val progressVolumes: Int?
        val mediaId = media.id.toString()
        if (mediaListStatuses.containsKey(mediaId)) {
            val mediaUpdate = mediaListStatuses[media.toString()]?.entry
            status = mediaUpdate?.status
            progress = mediaUpdate?.progress
            progressVolumes = mediaUpdate?.progressVolumes
        } else {
            status = media.mediaListEntry?.status
            progress = media.mediaListEntry?.progress
            progressVolumes = media.mediaListEntry?.progressVolumes
        }
        if (mediaStatusAware.mediaListStatus != status
            || mediaStatusAware.ignored != ignored
            || mediaStatusAware.progress != progress
            || mediaStatusAware.progressVolumes != progressVolumes
            || mediaStatusAware.showLessImportantTags != showLessImportantTags
            || mediaStatusAware.showSpoilerTags != showSpoilerTags
        ) {
            copiedEntry = entry.copyMedia(status, progress, progressVolumes, ignored, showLessImportantTags, showSpoilerTags)
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
