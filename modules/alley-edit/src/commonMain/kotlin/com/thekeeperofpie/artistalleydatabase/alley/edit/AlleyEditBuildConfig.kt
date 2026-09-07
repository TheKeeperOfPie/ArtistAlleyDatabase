package com.thekeeperofpie.artistalleydatabase.alley.edit

import com.thekeeperofpie.artistalleydatabase.alley.edit.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.utils.buildconfig.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class AlleyEditBuildConfig : BuildConfig {
    override val buildType = if (BuildKonfig.debug) "debug" else "release"
}
