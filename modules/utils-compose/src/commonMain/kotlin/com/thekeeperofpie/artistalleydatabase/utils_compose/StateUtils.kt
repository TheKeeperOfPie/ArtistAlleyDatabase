package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapMutableState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <T> observableStateOf(initialValue: T, crossinline onChange: (T) -> Unit) =
    object : ReadWriteProperty<Any?, T> {
        var value by mutableStateOf(initialValue)

        override operator fun getValue(thisRef: Any?, property: KProperty<*>) = value

        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            this.value = value
            onChange(value)
        }
    }

inline fun <reified T> SavedStateHandle.getMutableStateFlow(
    key: String,
    initialValue: () -> T,
): MutableStateFlow<T> {
    val existing = get<T>(key)
    val stateFlow = getStateFlow(key, existing ?: initialValue())
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

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
class SavedStateStateFlowWrapper<T>(
    private val key: String,
    private val savedStateHandle: SavedStateHandle,
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
