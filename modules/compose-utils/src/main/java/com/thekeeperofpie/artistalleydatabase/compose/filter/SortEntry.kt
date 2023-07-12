package com.thekeeperofpie.artistalleydatabase.compose.filter

import kotlin.reflect.KClass

data class SortEntry<T : SortOption>(
    override val value: T,
    override val state: FilterIncludeExcludeState = FilterIncludeExcludeState.DEFAULT,
) : FilterEntry<T> {
    companion object {
        fun <T : SortOption> options(enumClass: KClass<T>) =
            enumClass.java.enumConstants!!.map(::SortEntry)
    }
}

fun <T : SortOption> List<SortEntry<T>>.selectedOption(default: T) =
    firstOrNull { it.state == FilterIncludeExcludeState.INCLUDE }
        ?.value
        ?: default
