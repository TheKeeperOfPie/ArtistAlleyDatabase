package com.thekeeperofpie.artistalleydatabase.anime.activity

import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_activity_type_anime_list
import artistalleydatabase.modules.anime.generated.resources.anime_activity_type_manga_list
import artistalleydatabase.modules.anime.generated.resources.anime_activity_type_media_list
import artistalleydatabase.modules.anime.generated.resources.anime_activity_type_message
import artistalleydatabase.modules.anime.generated.resources.anime_activity_type_text
import artistalleydatabase.modules.anime.generated.resources.anime_activity_type_unknown
import com.anilist.data.UserSocialActivityQuery
import com.anilist.data.type.ActivityType
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
        ActivityType.TEXT -> Res.string.anime_activity_type_text
        ActivityType.ANIME_LIST -> Res.string.anime_activity_type_anime_list
        ActivityType.MANGA_LIST -> Res.string.anime_activity_type_manga_list
        ActivityType.MESSAGE -> Res.string.anime_activity_type_message
        ActivityType.MEDIA_LIST -> Res.string.anime_activity_type_media_list
        ActivityType.UNKNOWN__ -> Res.string.anime_activity_type_unknown
    }
}
