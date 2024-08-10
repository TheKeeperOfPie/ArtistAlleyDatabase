package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <T> observableStateOf(value: T, crossinline onChange: (T) -> Unit) =
    object : ReadWriteProperty<Any?, T> {
        var value by mutableStateOf(value)

        override operator fun getValue(thisRef: Any?, property: KProperty<*>) = value

        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            this.value = value
            onChange(value)
        }
    }
