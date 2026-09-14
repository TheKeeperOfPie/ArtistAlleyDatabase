package com.thekeeperofpie.artistalleydatabase.alley.desktop

import com.thekeeperofpie.artistalleydatabase.utils.buildconfig.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
object DesktopBuildConfig : BuildConfig {
    override val buildType = "debug"
}
