package com.thekeeperofpie.artistalleydatabase.anime.activity

import com.anilist.UserSocialActivityQuery

object ActivityUtils {

    fun UserSocialActivityQuery.Data.Page.Activity.isAdult() = when (this) {
        is UserSocialActivityQuery.Data.Page.ListActivityActivity -> media?.isAdult != false
        is UserSocialActivityQuery.Data.Page.MessageActivityActivity -> false
        is UserSocialActivityQuery.Data.Page.OtherActivity -> false
        is UserSocialActivityQuery.Data.Page.TextActivityActivity -> false
    }
}
