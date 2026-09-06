package com.thekeeperofpie.artistalleydatabase.alley.web

import artistalleydatabase.alley_web.generated.resources.Res
import com.thekeeperofpie.artistalleydatabase.alley.settings.AboutLibrariesProvider

internal object AlleyAppAboutLibrariesProvider : AboutLibrariesProvider {
    override suspend fun readBytes() = Res.readBytes("files/aboutlibraries.json")
}
