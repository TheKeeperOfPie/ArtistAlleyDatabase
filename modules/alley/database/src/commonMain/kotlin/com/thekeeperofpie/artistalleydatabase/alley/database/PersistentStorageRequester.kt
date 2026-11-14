package com.thekeeperofpie.artistalleydatabase.alley.database

expect object PersistentStorageRequester {
    suspend fun requestPersistent()
}
