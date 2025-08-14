package com.thekeeperofpie.artistalleydatabase.utils_compose.state

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.enums.enumEntries

@OptIn(InternalSerializationApi::class)
object StateUtils {

    inline fun <reified T : @Serializable Any> jsonSaver(): Saver<T, String> {
        val serializer = T::class.serializer()
        return Saver(
            save = { Json.encodeToString(serializer, it) },
            restore = { Json.decodeFromString(serializer, it) },
        )
    }
}
