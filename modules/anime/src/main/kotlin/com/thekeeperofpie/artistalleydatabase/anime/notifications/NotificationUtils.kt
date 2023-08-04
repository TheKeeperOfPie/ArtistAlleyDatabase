package com.thekeeperofpie.artistalleydatabase.anime.notifications

import com.anilist.NotificationsQuery.Data.Page

val Page.Notification.id
    get() = when (this) {
        is Page.ActivityMentionNotificationNotification -> id
        is Page.ActivityMessageNotificationNotification -> id
        is Page.AiringNotificationNotification -> id
        is Page.FollowingNotificationNotification -> id
        is Page.ActivityReplyNotificationNotification -> id
        is Page.ActivityReplySubscribedNotificationNotification -> id
        is Page.ActivityLikeNotificationNotification -> id
        is Page.ActivityReplyLikeNotificationNotification -> id
        is Page.RelatedMediaAdditionNotificationNotification -> id
        is Page.MediaDataChangeNotificationNotification -> id
        is Page.MediaDeletionNotificationNotification -> id
        is Page.MediaMergeNotificationNotification -> id
        is Page.OtherNotification -> null
    }

val Page.Notification.activityId
    get() = when (this) {
        is Page.ActivityLikeNotificationNotification -> activityId.toString()
        is Page.ActivityMentionNotificationNotification -> activityId.toString()
        is Page.ActivityMessageNotificationNotification -> activityId.toString()
        is Page.ActivityReplyLikeNotificationNotification -> activityId.toString()
        is Page.ActivityReplyNotificationNotification -> activityId.toString()
        is Page.ActivityReplySubscribedNotificationNotification -> activityId.toString()
        is Page.AiringNotificationNotification,
        is Page.FollowingNotificationNotification,
        is Page.RelatedMediaAdditionNotificationNotification,
        is Page.MediaDataChangeNotificationNotification,
        is Page.MediaDeletionNotificationNotification,
        is Page.MediaMergeNotificationNotification,
        is Page.OtherNotification,
        -> null
    }

val Page.Notification.mediaId
    get() = when (this) {
        is Page.AiringNotificationNotification -> animeId.toString()
        is Page.RelatedMediaAdditionNotificationNotification -> mediaId.toString()
        is Page.MediaDataChangeNotificationNotification -> mediaId.toString()
        is Page.MediaMergeNotificationNotification -> mediaId.toString()
        is Page.ActivityLikeNotificationNotification,
        is Page.ActivityMentionNotificationNotification,
        is Page.ActivityMessageNotificationNotification,
        is Page.ActivityReplyLikeNotificationNotification,
        is Page.ActivityReplyNotificationNotification,
        is Page.ActivityReplySubscribedNotificationNotification,
        is Page.MediaDeletionNotificationNotification,
        is Page.FollowingNotificationNotification,
        is Page.OtherNotification,
        -> null
    }
