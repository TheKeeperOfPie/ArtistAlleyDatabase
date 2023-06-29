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
