package com.thekeeperofpie.artistalleydatabase.anime.notifications

import com.anilist.data.NotificationMediaAndActivityQuery
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityStatusAware

data class NotificationActivityEntry(
    val id: String,
    val activity: NotificationMediaAndActivityQuery.Data.Activity.Activity,
    override val liked: Boolean,
    override val subscribed: Boolean,
) : ActivityStatusAware
