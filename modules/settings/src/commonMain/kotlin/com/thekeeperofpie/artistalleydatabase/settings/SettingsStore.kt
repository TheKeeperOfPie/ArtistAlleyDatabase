package com.thekeeperofpie.artistalleydatabase.settings

expect class SettingsStore {

    fun writeString(key: String, value: String, commitImmediately: Boolean = false)
    fun readString(key: String): String?
}
