package com.thekeeperofpie.artistalleydatabase.utils.buildconfig

import androidx.compose.runtime.staticCompositionLocalOf

interface BuildConfig {
    val isDebug: Boolean get() = buildType == "debug"
    val buildType: String
}

val LocalBuildConfig = staticCompositionLocalOf<BuildConfig> { FakeBuildConfig }

private val FakeBuildConfig = object : BuildConfig {
    override val buildType: String = "release"
}
