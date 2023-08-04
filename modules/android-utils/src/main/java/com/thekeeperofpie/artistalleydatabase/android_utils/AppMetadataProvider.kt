package com.thekeeperofpie.artistalleydatabase.android_utils

import androidx.annotation.DrawableRes

interface AppMetadataProvider {

    val versionCode: Int
    val versionName: String

    @get:DrawableRes
    val appIconDrawableRes: Int
}
