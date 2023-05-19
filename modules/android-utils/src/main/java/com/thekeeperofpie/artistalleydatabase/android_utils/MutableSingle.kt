package com.thekeeperofpie.artistalleydatabase.android_utils

import kotlin.reflect.KProperty

data class MutableSingle<Value>(var value: Value)

@Suppress("NOTHING_TO_INLINE")
inline operator fun <Value> MutableSingle<Value>.setValue(
    thisObj: Any?,
    property: KProperty<*>,
    value: Value
) {
    this.value = value
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <Value> MutableSingle<Value>.getValue(
    nothing: Nothing?,
    property: KProperty<*>
) = value
