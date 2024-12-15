package com.thekeeperofpie.artistalleydatabase.anime.activities.data

import com.anilist.data.UserSocialActivityQuery
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaFilterableData

interface ActivityEntryProvider<ActivityEntry, MediaEntry> {
    /** Proxies to a real type to decouple the media data class from recommendations */
    fun activityEntry(activity: UserSocialActivityQuery.Data.Page.Activity): ActivityEntry
    fun id(entry: ActivityEntry): String
    fun contentType(entry: ActivityEntry): String
    fun activity(entry: ActivityEntry): UserSocialActivityQuery.Data.Page.Activity
    fun activityStatusAware(entry: ActivityEntry): ActivityStatusAware
    fun media(entry: ActivityEntry): MediaEntry?
    fun mediaFilterable(entry: ActivityEntry): MediaFilterableData?
    fun copyActivityEntry(entry: ActivityEntry, mediaEntry: MediaEntry?): ActivityEntry
    fun copyActivityEntry(entry: ActivityEntry, liked: Boolean, subscribed: Boolean): ActivityEntry
}
