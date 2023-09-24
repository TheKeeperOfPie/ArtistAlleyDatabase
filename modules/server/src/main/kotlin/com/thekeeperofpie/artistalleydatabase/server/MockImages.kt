package com.thekeeperofpie.artistalleydatabase.server

import java.awt.Color

object MockImages {

    fun bannerImage(id: Int) = "com.thekeeperofpie.artistalleydatabase.test://image/banner/$id"
    fun coverImage(id: Int) = "com.thekeeperofpie.artistalleydatabase.test://image/cover/$id"
    fun coverImageColor(id: Int) = Color(id.hashCode()).run {
        String.format("#%06x", rgb and 0xFFFFFF)
    }
}
