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

typealias ComposeSaver<Original, Saveable> = androidx.compose.runtime.saveable.Saver<Original, Saveable>

@OptIn(InternalSerializationApi::class)
object StateUtils {

    /**
     * [androidx.compose.runtime.saveable.rememberSaveable] doesn't support [Enum] values by
     * default. This serializes null as an empty string, since returning null directly means that
     * the state will be re-initialized on restore, which isn't desirable if the value can be
     * set to null by the user.
     */
    inline fun <reified T : Enum<T>> nullableEnumSaver() = Saver<T?, String>(
        save = { it?.name.orEmpty() },
        restore = { value ->
            if (value.isEmpty()) {
                null
            } else {
                enumEntries<T>().first { it.name == value }
            }
        },
    )

    inline fun <reified T : @Serializable Any> jsonSaver(): Saver<T, String> {
        val serializer = T::class.serializer()
        return Saver(
            save = { Json.encodeToString(serializer, it) },
            restore = { Json.decodeFromString(serializer, it) },
        )
    }

    inline fun <reified T : Any> listJsonSaver(): Saver<List<T>, String> {
        val listSerializer = ListSerializer(T::class.serializer())
        return Saver(
            save = { Json.encodeToString(listSerializer, it) },
            restore = { Json.decodeFromString(listSerializer, it) },
        )
    }

    inline fun <reified T : Any> snapshotListJsonSaver(): Saver<SnapshotStateList<T>, String> {
        val listSerializer = ListSerializer(T::class.serializer())
        return Saver(
            save = { Json.encodeToString(listSerializer, it) },
            restore = { Json.decodeFromString(listSerializer, it).toMutableStateList() },
        )
    }
}

fun <T> SnapshotStateList<T>.swap(indexOne: Int, indexTwo: Int) {
    if (indexOne == indexTwo) {
        return
    }

    Snapshot.withMutableSnapshot {
        val temp = this[indexOne]
        this[indexOne] = this[indexTwo]
        this[indexTwo] = temp
    }
}
