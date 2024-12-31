package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.annotation.MainThread
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapMutableState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json

// TODO: Can this just use class name as prefix?
@MainThread
fun CreationExtras.createSavedStateHandle(scopeKey: String) =
    ScopedSavedStateHandle(scopeKey, createSavedStateHandle())

class ScopedSavedStateHandle(private val prefix: String, val savedStateHandle: SavedStateHandle) {

    @MainThread
    operator fun <T> get(key: String) = savedStateHandle.get<T>("$prefix-$key")

    @MainThread
    operator fun <T> set(key: String, value: T?) = savedStateHandle.set("$prefix-$key", value)

    @MainThread
    fun <T> getStateFlow(key: String, initialValue: T) =
        savedStateHandle.getStateFlow<T>("$prefix-$key", initialValue)
}

inline fun <reified T> ScopedSavedStateHandle.getMutableStateFlow(
    key: String,
    initialValue: () -> T,
): MutableStateFlow<T> {
    val existing = get<T>(key)
    val stateFlow = getStateFlow(key, existing ?: initialValue())
    return SavedStateStateFlowWrapper(key, this, stateFlow)
}

inline fun <reified T> ScopedSavedStateHandle.getMutableStateFlow(
    key: String,
    initialValue: T,
): MutableStateFlow<T> {
    val existing = get<T>(key)
    val stateFlow = getStateFlow(key, existing ?: initialValue)
    return SavedStateStateFlowWrapper(key, this, stateFlow)
}

context(ViewModel)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
inline fun <reified Input, Output> ScopedSavedStateHandle.getMutableStateFlow(
    key: String,
    initialValue: () -> Output,
    noinline serialize: (Output) -> Input,
    noinline deserialize: (Input) -> Output,
): MutableStateFlow<Output> = getMutableStateFlow(key) { serialize(initialValue()) }
    .mapMutableState(viewModelScope, deserialize, serialize)

context(ViewModel)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
inline fun <reified T> ScopedSavedStateHandle.getMutableStateFlow(
    json: Json,
    key: String,
    initialValue: () -> T,
): MutableStateFlow<T> = getMutableStateFlow(key) { json.encodeToString(initialValue()) }
    .mapMutableState(viewModelScope, json::decodeFromString, json::encodeToString)

context(ViewModel)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
inline fun <reified T> ScopedSavedStateHandle.getMutableStateFlow(
    json: Json,
    key: String,
    initialValue: T,
): MutableStateFlow<T> = getMutableStateFlow(key) { json.encodeToString(initialValue) }
    .mapMutableState(viewModelScope, json::decodeFromString, json::encodeToString)

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
class SavedStateStateFlowWrapper<T>(
    private val key: String,
    private val savedStateHandle: ScopedSavedStateHandle,
    private val stateFlow: StateFlow<T>,
) : MutableStateFlow<T>, StateFlow<T> by stateFlow {

    override var value: T
        get() = stateFlow.value
        set(value) {
            synchronized(savedStateHandle) {
                savedStateHandle[key] = value
            }
        }

    override fun compareAndSet(expect: T, update: T): Boolean {
        // TODO: Figure out a way to not sync around SavedStateHandle
        synchronized(savedStateHandle) {
            val current: T? = savedStateHandle[key]
            if (expect != current) return false
            savedStateHandle[key] = update
            return true
        }
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
