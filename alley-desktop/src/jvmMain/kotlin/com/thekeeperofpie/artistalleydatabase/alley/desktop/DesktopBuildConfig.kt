package com.thekeeperofpie.artistalleydatabase.alley.desktop

import com.thekeeperofpie.artistalleydatabase.utils.buildconfig.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class DesktopBuildConfig : BuildConfig {
    override val buildType = "debug"
}
