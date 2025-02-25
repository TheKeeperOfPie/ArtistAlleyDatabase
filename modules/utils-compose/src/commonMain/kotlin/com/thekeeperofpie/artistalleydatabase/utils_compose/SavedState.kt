package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapMutableState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

inline fun <reified T> SavedStateHandle.getMutableStateFlow(
    key: String,
    initialValue: () -> T,
): MutableStateFlow<T> {
    val existing = get<T>(key)
    val stateFlow = getStateFlow(key, existing ?: initialValue())
    return SavedStateStateFlowWrapper(key, this, stateFlow)
}

inline fun <reified T> SavedStateHandle.getMutableStateFlow(
    key: String,
    initialValue: T,
): MutableStateFlow<T> {
    val existing = get<T>(key)
    val stateFlow = getStateFlow(key, existing ?: initialValue)
    return SavedStateStateFlowWrapper(key, this, stateFlow)
}

context(ViewModel)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
inline fun <reified Input, Output> SavedStateHandle.getMutableStateFlow(
    key: String,
    initialValue: () -> Output,
    noinline serialize: (Output) -> Input,
    noinline deserialize: (Input) -> Output,
): MutableStateFlow<Output> = getMutableStateFlow(key) { serialize(initialValue()) }
    .mapMutableState(viewModelScope, deserialize, serialize)

context(ViewModel)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
inline fun <reified T> SavedStateHandle.getMutableStateFlow(
    json: Json,
    key: String,
    initialValue: () -> T,
): MutableStateFlow<T> = getMutableStateFlow(key) { json.encodeToString(initialValue()) }
    .mapMutableState(viewModelScope, json::decodeFromString, json::encodeToString)

context(ViewModel)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
inline fun <reified T> SavedStateHandle.getMutableStateFlow(
    json: Json,
    key: String,
    initialValue: T,
): MutableStateFlow<T> = getMutableStateFlow(key) { json.encodeToString(initialValue) }
    .mapMutableState(viewModelScope, json::decodeFromString, json::encodeToString)

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
class SavedStateStateFlowWrapper<T>(
    private val key: String,
    private val savedStateHandle: SavedStateHandle,
    private val stateFlow: StateFlow<T>,
) : MutableStateFlow<T>, StateFlow<T> by stateFlow {

    override var value: T
        get() = stateFlow.value
        set(value) {
            savedStateHandle[key] = value
        }

    override fun compareAndSet(expect: T, update: T): Boolean {
        // TODO: Synchronization was removed for wasmJs
        val current: T? = savedStateHandle[key]
        if (expect != current) return false
        savedStateHandle[key] = update
        return true
    }

    // TODO: Violates contract
    override val subscriptionCount = MutableStateFlow(1)

    override suspend fun emit(value: T) {
        savedStateHandle[key] = value
    }

    override fun tryEmit(value: T): Boolean {
        savedStateHandle[key] = value
        return true
    }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        throw UnsupportedOperationException("MutableStateFlow.resetReplayCache is not supported")
    }
}
