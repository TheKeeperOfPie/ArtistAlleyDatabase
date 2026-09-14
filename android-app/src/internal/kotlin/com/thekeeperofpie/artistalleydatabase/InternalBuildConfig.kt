package com.thekeeperofpie.artistalleydatabase

import com.thekeeperofpie.artistalleydatabase.utils.buildconfig.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
internal object InternalBuildConfig : BuildConfig {
    override val buildType = "internal"
}
