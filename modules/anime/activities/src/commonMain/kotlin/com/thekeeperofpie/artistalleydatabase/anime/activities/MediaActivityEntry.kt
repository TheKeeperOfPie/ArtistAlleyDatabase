package com.thekeeperofpie.artistalleydatabase.anime.activities

import com.anilist.data.fragment.ListActivityMediaListActivityItem

data class MediaActivityEntry(
    val activity: ListActivityMediaListActivityItem,
    val activityId: String = activity.id.toString(),
    override val liked: Boolean = activity.isLiked ?: false,
    override val subscribed: Boolean = activity.isSubscribed ?: false,
) : ActivityStatusAware