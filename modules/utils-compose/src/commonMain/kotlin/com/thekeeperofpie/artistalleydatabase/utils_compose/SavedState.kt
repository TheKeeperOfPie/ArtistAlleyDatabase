package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapMutableState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

fun <T> SavedStateHandle.getOrPut(key: String, defaultValue: () -> T): T {
    val existing = get<T>(key)
    if (existing != null) {
        return existing
    }
    val value = defaultValue()
    this[key] = value
    return value
}

context(ViewModel)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
inline fun <reified Input, Output> SavedStateHandle.getMutableStateFlow(
    key: String,
    initialValue: () -> Output,
    noinline serialize: (Output) -> Input,
    noinline deserialize: (Input) -> Output,
): MutableStateFlow<Output> = getMutableStateFlow(key, serialize(initialValue()))
    .mapMutableState(viewModelScope, deserialize, serialize)

context(ViewModel)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
inline fun <reified T> SavedStateHandle.getMutableStateFlow(
    json: Json,
    key: String,
    initialValue: () -> T,
): MutableStateFlow<T> = getMutableStateFlow(key, json.encodeToString(initialValue()))
    .mapMutableState(viewModelScope, json::decodeFromString, json::encodeToString)

context(ViewModel)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
inline fun <reified T> SavedStateHandle.getMutableStateFlow(
    json: Json,
    key: String,
    initialValue: T,
): MutableStateFlow<T> = getMutableStateFlow(key, json.encodeToString(initialValue))
    .mapMutableState(viewModelScope, json::decodeFromString, json::encodeToString)
