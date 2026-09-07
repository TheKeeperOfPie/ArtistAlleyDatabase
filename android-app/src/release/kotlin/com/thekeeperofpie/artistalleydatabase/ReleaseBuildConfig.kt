package com.thekeeperofpie.artistalleydatabase

import com.thekeeperofpie.artistalleydatabase.utils.buildconfig.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
internal class ReleaseBuildConfig : BuildConfig {
    override val buildType = "release"
}
