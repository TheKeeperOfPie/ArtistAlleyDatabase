package com.thekeeperofpie.artistalleydatabase.anime.notifications

import com.anilist.data.NotificationsQuery
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

data class NotificationEntry<MediaEntry, ForumCommentEntry>(
    val notificationId: EntryId,
    val notification: NotificationsQuery.Data.Page.Notification,
    val activityEntry: NotificationActivityEntry?,
    val mediaEntry: MediaEntry?,
    val commentEntry: ForumCommentEntry?,
)
