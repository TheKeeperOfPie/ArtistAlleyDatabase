package com.thekeeperofpie.artistalleydatabase.alley.form

import com.thekeeperofpie.artistalleydatabase.alley.form.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.utils.buildconfig.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
object AlleyFormBuildConfig : BuildConfig {
    override val buildType = if (BuildKonfig.debug) "debug" else "release"
}
