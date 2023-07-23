package com.thekeeperofpie.artistalleydatabase.anime.activity

import com.anilist.UserSocialActivityQuery
import com.anilist.type.ActivityType
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

object ActivityUtils {

    fun UserSocialActivityQuery.Data.Page.Activity.isAdult() = when (this) {
        is UserSocialActivityQuery.Data.Page.ListActivityActivity -> media?.isAdult != false
        is UserSocialActivityQuery.Data.Page.MessageActivityActivity -> false
        is UserSocialActivityQuery.Data.Page.OtherActivity -> false
        is UserSocialActivityQuery.Data.Page.TextActivityActivity -> false
    }

    val UserSocialActivityQuery.Data.Page.Activity.entryId: EntryId
        get() = when (this) {
            is UserSocialActivityQuery.Data.Page.ListActivityActivity -> EntryId("list", this.id.toString())
            is UserSocialActivityQuery.Data.Page.MessageActivityActivity -> EntryId("message", this.id.toString())
            is UserSocialActivityQuery.Data.Page.TextActivityActivity -> EntryId("text", this.id.toString())
            is UserSocialActivityQuery.Data.Page.OtherActivity -> EntryId("other", System.identityHashCode(this).toString())
        }

    val UserSocialActivityQuery.Data.Page.Activity.liked: Boolean
        get() = when (this) {
            is UserSocialActivityQuery.Data.Page.ListActivityActivity -> this.isLiked
            is UserSocialActivityQuery.Data.Page.MessageActivityActivity -> this.isLiked
            is UserSocialActivityQuery.Data.Page.TextActivityActivity -> this.isLiked
            is UserSocialActivityQuery.Data.Page.OtherActivity -> false
        } ?: false

    val UserSocialActivityQuery.Data.Page.Activity.subscribed: Boolean
        get() = when (this) {
            is UserSocialActivityQuery.Data.Page.ListActivityActivity -> this.isSubscribed
            is UserSocialActivityQuery.Data.Page.MessageActivityActivity -> this.isSubscribed
            is UserSocialActivityQuery.Data.Page.TextActivityActivity -> this.isSubscribed
            is UserSocialActivityQuery.Data.Page.OtherActivity -> false
        } ?: false

    fun ActivityType.toTextRes() = when (this) {
        ActivityType.TEXT -> R.string.anime_activity_type_text
        ActivityType.ANIME_LIST -> R.string.anime_activity_type_anime_list
        ActivityType.MANGA_LIST -> R.string.anime_activity_type_manga_list
        ActivityType.MESSAGE -> R.string.anime_activity_type_message
        ActivityType.MEDIA_LIST -> R.string.anime_activity_type_media_list
        ActivityType.UNKNOWN__ -> R.string.anime_activity_type_unknown
    }
}
