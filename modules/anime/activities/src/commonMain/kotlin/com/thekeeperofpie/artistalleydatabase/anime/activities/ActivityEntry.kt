package com.thekeeperofpie.artistalleydatabase.anime.activities

import com.anilist.data.UserSocialActivityQuery
import com.anilist.data.fragment.MediaCompactWithTags
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityUtils.entryId
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityUtils.liked
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityUtils.subscribed
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

data class ActivityEntry<MediaEntry>(
    val activityId: EntryId,
    val activity: UserSocialActivityQuery.Data.Page.Activity,
    override val liked: Boolean,
    override val subscribed: Boolean,
    val media: MediaEntry?,
) : ActivityStatusAware {
    constructor(
        activity: UserSocialActivityQuery.Data.Page.Activity,
        mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
    ) : this(
        activityId = activity.entryId,
        activity = activity,
        liked = activity.liked,
        subscribed = activity.subscribed,
        media = (activity as? UserSocialActivityQuery.Data.Page.ListActivityActivity)?.media
            ?.let(mediaEntryProvider::mediaEntry),
    )
}
