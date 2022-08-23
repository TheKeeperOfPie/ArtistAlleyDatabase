package com.thekeeperofpie.artistalleydatabase.android_utils

fun <T> Iterable<T>.split(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
    val filtered = mutableListOf<T>()
    val remaining = mutableListOf<T>()
    forEach {
        if (predicate(it)) {
            filtered.add(it)
        } else {
            remaining.add(it)
        }
    }
    return filtered to remaining
}