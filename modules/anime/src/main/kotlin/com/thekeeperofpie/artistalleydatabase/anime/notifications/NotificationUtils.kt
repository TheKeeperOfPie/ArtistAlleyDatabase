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
        is Page.ActivityLikeNotificationNotification -> {
            when (val activity = activity) {
                is Page.ActivityLikeNotificationNotification.ListActivityActivity -> activity.id.toString()
                is Page.ActivityLikeNotificationNotification.MessageActivityActivity -> activity.id.toString()
                is Page.ActivityLikeNotificationNotification.TextActivityActivity -> activity.id.toString()
                is Page.ActivityLikeNotificationNotification.OtherActivity,
                null,
                -> null
            }
        }
        is Page.ActivityMentionNotificationNotification -> {
            when (val activity = activity) {
                is Page.ActivityMentionNotificationNotification.ListActivityActivity -> activity.id.toString()
                is Page.ActivityMentionNotificationNotification.MessageActivityActivity -> activity.id.toString()
                is Page.ActivityMentionNotificationNotification.TextActivityActivity -> activity.id.toString()
                is Page.ActivityMentionNotificationNotification.OtherActivity,
                null,
                -> null
            }
        }
        is Page.ActivityMessageNotificationNotification -> message?.id?.toString()
        is Page.ActivityReplyLikeNotificationNotification -> {
            when (val activity = activity) {
                is Page.ActivityReplyLikeNotificationNotification.ListActivityActivity -> activity.id.toString()
                is Page.ActivityReplyLikeNotificationNotification.MessageActivityActivity -> activity.id.toString()
                is Page.ActivityReplyLikeNotificationNotification.TextActivityActivity -> activity.id.toString()
                is Page.ActivityReplyLikeNotificationNotification.OtherActivity,
                null,
                -> null
            }
        }
        is Page.ActivityReplyNotificationNotification -> {
            when (val activity = activity) {
                is Page.ActivityReplyNotificationNotification.ListActivityActivity -> activity.id.toString()
                is Page.ActivityReplyNotificationNotification.MessageActivityActivity -> activity.id.toString()
                is Page.ActivityReplyNotificationNotification.TextActivityActivity -> activity.id.toString()
                is Page.ActivityReplyNotificationNotification.OtherActivity,
                null,
                -> null
            }
        }
        is Page.ActivityReplySubscribedNotificationNotification -> {
            when (val activity = activity) {
                is Page.ActivityReplySubscribedNotificationNotification.ListActivityActivity -> activity.id.toString()
                is Page.ActivityReplySubscribedNotificationNotification.MessageActivityActivity -> activity.id.toString()
                is Page.ActivityReplySubscribedNotificationNotification.TextActivityActivity -> activity.id.toString()
                is Page.ActivityReplySubscribedNotificationNotification.OtherActivity,
                null,
                -> null
            }
        }
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
        is Page.ActivityLikeNotificationNotification ->
            when (val activity = activity) {
                is Page.ActivityLikeNotificationNotification.ListActivityActivity -> activity.media?.id?.toString()
                is Page.ActivityLikeNotificationNotification.MessageActivityActivity,
                is Page.ActivityLikeNotificationNotification.OtherActivity,
                is Page.ActivityLikeNotificationNotification.TextActivityActivity,
                null,
                -> null
            }
        is Page.ActivityMentionNotificationNotification ->
            when (val activity = activity) {
                is Page.ActivityMentionNotificationNotification.ListActivityActivity -> activity.media?.id?.toString()
                is Page.ActivityMentionNotificationNotification.MessageActivityActivity,
                is Page.ActivityMentionNotificationNotification.OtherActivity,
                is Page.ActivityMentionNotificationNotification.TextActivityActivity,
                null,
                -> null
            }
        is Page.ActivityMessageNotificationNotification -> null
        is Page.ActivityReplyLikeNotificationNotification ->
            when (val activity = activity) {
                is Page.ActivityReplyLikeNotificationNotification.ListActivityActivity -> activity.media?.id?.toString()
                is Page.ActivityReplyLikeNotificationNotification.MessageActivityActivity,
                is Page.ActivityReplyLikeNotificationNotification.OtherActivity,
                is Page.ActivityReplyLikeNotificationNotification.TextActivityActivity,
                null,
                -> null
            }
        is Page.ActivityReplyNotificationNotification ->
            when (val activity = activity) {
                is Page.ActivityReplyNotificationNotification.ListActivityActivity -> activity.media?.id?.toString()
                is Page.ActivityReplyNotificationNotification.MessageActivityActivity,
                is Page.ActivityReplyNotificationNotification.OtherActivity,
                is Page.ActivityReplyNotificationNotification.TextActivityActivity,
                null,
                -> null
            }
        is Page.ActivityReplySubscribedNotificationNotification ->
            when (val activity = activity) {
                is Page.ActivityReplySubscribedNotificationNotification.ListActivityActivity -> activity.media?.id?.toString()
                is Page.ActivityReplySubscribedNotificationNotification.MessageActivityActivity,
                is Page.ActivityReplySubscribedNotificationNotification.OtherActivity,
                is Page.ActivityReplySubscribedNotificationNotification.TextActivityActivity,
                null,
                -> null
            }
        is Page.AiringNotificationNotification -> media?.id?.toString()
        is Page.RelatedMediaAdditionNotificationNotification -> media?.id?.toString()
        is Page.MediaDataChangeNotificationNotification -> media?.id?.toString()
        is Page.MediaMergeNotificationNotification -> media?.id?.toString()
        is Page.MediaDeletionNotificationNotification,
        is Page.FollowingNotificationNotification,
        is Page.OtherNotification,
        -> null
    }
