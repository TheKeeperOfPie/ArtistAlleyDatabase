package com.thekeeperofpie.artistalleydatabase.alley.edit.data

expect class AlleyEditRemoteDatabase {
    suspend fun loadFunction(): String
}
