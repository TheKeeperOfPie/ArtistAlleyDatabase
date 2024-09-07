package com.thekeeperofpie.artistalleydatabase.utils_compose

interface AppMetadataProvider {
    val versionCode: Int
    val versionName: String
    // TODO: Making this Composable breaks the compiler, figure out a better way to do this
    val appDrawableModel: Any
}
