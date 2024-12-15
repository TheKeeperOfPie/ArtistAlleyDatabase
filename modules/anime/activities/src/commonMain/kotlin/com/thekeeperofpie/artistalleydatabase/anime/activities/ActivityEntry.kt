package com.thekeeperofpie.artistalleydatabase.anime.activities

import com.anilist.data.UserSocialActivityQuery
import com.anilist.data.fragment.MediaCompactWithTags
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityUtils.entryId
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityUtils.liked
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityUtils.subscribed
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

data class ActivityEntry<MediaEntry>(
    val activityId: EntryId,
    val activity: UserSocialActivityQuery.Data.Page.Activity,
    override val liked: Boolean,
    override val subscribed: Boolean,
    val media: MediaEntry?,
) : ActivityStatusAware {
    companion object {
        fun <MediaEntry> provider(
            mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
        ) = object : ActivityEntryProvider<ActivityEntry<MediaEntry>, MediaEntry> {
            override fun activityEntry(activity: UserSocialActivityQuery.Data.Page.Activity) =
                ActivityEntry(activity, mediaEntryProvider)

            override fun id(entry: ActivityEntry<MediaEntry>) = entry.activityId.scopedId.toString()
            override fun contentType(entry: ActivityEntry<MediaEntry>) =
                entry.activityId.type.toString()

            override fun activity(entry: ActivityEntry<MediaEntry>) = entry.activity
            override fun activityStatusAware(entry: ActivityEntry<MediaEntry>) = entry
            override fun media(entry: ActivityEntry<MediaEntry>) = entry.media
            override fun mediaFilterable(entry: ActivityEntry<MediaEntry>) =
                entry.media?.let(mediaEntryProvider::mediaFilterable)

            override fun copyActivityEntry(
                entry: ActivityEntry<MediaEntry>,
                mediaEntry: MediaEntry?,
            ) = entry.copy(media = mediaEntry)

            override fun copyActivityEntry(
                entry: ActivityEntry<MediaEntry>,
                liked: Boolean,
                subscribed: Boolean,
            ) = entry.copy(liked = liked, subscribed = subscribed)
        }
    }

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
