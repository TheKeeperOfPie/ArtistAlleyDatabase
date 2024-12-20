package com.thekeeperofpie.artistalleydatabase.anime.notifications

import com.anilist.data.NotificationsQuery.Data.Page

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
        is Page.ThreadCommentMentionNotificationNotification -> id
        is Page.ThreadCommentLikeNotificationNotification -> id
        is Page.ThreadCommentReplyNotificationNotification -> id
        is Page.ThreadCommentSubscribedNotificationNotification -> id
        is Page.ThreadLikeNotificationNotification -> id
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
        is Page.ThreadCommentMentionNotificationNotification,
        is Page.ThreadCommentLikeNotificationNotification,
        is Page.ThreadCommentReplyNotificationNotification,
        is Page.ThreadCommentSubscribedNotificationNotification,
        is Page.ThreadLikeNotificationNotification,
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
        is Page.ThreadCommentMentionNotificationNotification,
        is Page.ThreadCommentLikeNotificationNotification,
        is Page.ThreadCommentReplyNotificationNotification,
        is Page.ThreadCommentSubscribedNotificationNotification,
        is Page.ThreadLikeNotificationNotification,
        is Page.OtherNotification,
        -> null
    }

val Page.Notification.comment
    get() = when (this) {
        is Page.ThreadCommentMentionNotificationNotification -> comment
        is Page.ThreadCommentLikeNotificationNotification -> comment
        is Page.ThreadCommentReplyNotificationNotification -> comment
        is Page.ThreadCommentSubscribedNotificationNotification -> comment
        is Page.ThreadLikeNotificationNotification,
        is Page.ActivityLikeNotificationNotification,
        is Page.ActivityMentionNotificationNotification,
        is Page.ActivityMessageNotificationNotification,
        is Page.ActivityReplyLikeNotificationNotification,
        is Page.ActivityReplyNotificationNotification,
        is Page.ActivityReplySubscribedNotificationNotification,
        is Page.AiringNotificationNotification,
        is Page.FollowingNotificationNotification,
        is Page.MediaDataChangeNotificationNotification,
        is Page.MediaDeletionNotificationNotification,
        is Page.MediaMergeNotificationNotification,
        is Page.OtherNotification,
        is Page.RelatedMediaAdditionNotificationNotification,
        -> null
    }
