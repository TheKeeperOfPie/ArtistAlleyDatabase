package com.thekeeperofpie.artistalleydatabase.alley.settings

import artistalleydatabase.modules.alley.generated.resources.Res

internal object AlleyAboutLibrariesProvider : AboutLibrariesProvider {
    override suspend fun readBytes() = Res.readBytes("files/aboutlibraries.json")
}
