package com.thekeeperofpie.artistalleydatabase.compose.filter

import kotlin.reflect.KClass

data class SortEntry<T : SortOption>(
    override val value: T,
    override val state: FilterIncludeExcludeState = FilterIncludeExcludeState.DEFAULT,
) : FilterEntry<T> {
    companion object {
        fun <T : SortOption> options(enumClass: KClass<T>, defaultEnabled: T?) =
            enumClass.java.enumConstants!!.map(::SortEntry).map {
                if (it.value == defaultEnabled) {
                    it.copy(state = FilterIncludeExcludeState.INCLUDE)
                } else {
                    it
                }
            }
    }
}

fun <T : SortOption> List<SortEntry<T>>.selectedOption(default: T) =
    firstOrNull { it.state == FilterIncludeExcludeState.INCLUDE }
        ?.value
        ?: default
