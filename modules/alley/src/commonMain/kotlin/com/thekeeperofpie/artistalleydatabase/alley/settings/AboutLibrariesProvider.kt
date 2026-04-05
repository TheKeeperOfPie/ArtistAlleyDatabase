package com.thekeeperofpie.artistalleydatabase.alley.settings

interface AboutLibrariesProvider {
    suspend fun readBytes(): ByteArray
}
