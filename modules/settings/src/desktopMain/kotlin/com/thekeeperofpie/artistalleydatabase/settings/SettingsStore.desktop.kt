package com.thekeeperofpie.artistalleydatabase.settings

actual class SettingsStore {

    actual fun writeString(
        key: String,
        value: String,
        commitImmediately: Boolean,
    ) {
        TODO("Not yet implemented")
    }

    actual fun readString(key: String): String? {
        TODO("Not yet implemented")
    }
}
