package com.thekeeperofpie.artistalleydatabase.anime.activity

import com.anilist.UserSocialActivityQuery
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.entryId
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.liked
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.subscribed
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

data class ActivityEntry(
    val activityId: EntryId,
    val activity: UserSocialActivityQuery.Data.Page.Activity,
    override val liked: Boolean,
    override val subscribed: Boolean,
    val media: MediaCompactWithTagsEntry?,
) : ActivityStatusAware {
    constructor(activity: UserSocialActivityQuery.Data.Page.Activity) : this(
        activityId = activity.entryId,
        activity = activity,
        liked = activity.liked,
        subscribed = activity.subscribed,
        media = (activity as? UserSocialActivityQuery.Data.Page.ListActivityActivity)?.media
            ?.let(::MediaCompactWithTagsEntry),
    )
}
