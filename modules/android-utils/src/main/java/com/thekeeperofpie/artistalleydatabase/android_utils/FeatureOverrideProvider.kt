package com.thekeeperofpie.artistalleydatabase.android_utils

interface FeatureOverrideProvider {
    val isReleaseBuild: Boolean

    // Disable Media3 cache, which allows tests to run without resetting cache dir
    val enableAppMediaPlayerCache: Boolean
}
