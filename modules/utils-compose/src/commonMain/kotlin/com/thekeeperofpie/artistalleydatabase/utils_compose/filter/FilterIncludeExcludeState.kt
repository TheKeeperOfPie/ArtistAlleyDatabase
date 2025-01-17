package com.thekeeperofpie.artistalleydatabase.utils_compose.filter

import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState.values


enum class FilterIncludeExcludeState {
    DEFAULT, INCLUDE, EXCLUDE;

    companion object {

        fun <T : Any?> toState(value: T, included: Collection<T>, excluded: Collection<T>) = when {
            included.contains(value) -> INCLUDE
            excluded.contains(value) -> EXCLUDE
            else -> DEFAULT
        }

        fun <Base, StateHolder, Comparison> applyFiltering(
            filters: List<StateHolder>,
            list: List<Base>,
            state: (StateHolder) -> FilterIncludeExcludeState,
            key: (StateHolder) -> Comparison,
            transform: (Base) -> List<Comparison>,
            transformIncludes: ((Base) -> List<Comparison>)? = null,
            mustContainAll: Boolean = true,
        ) = applyFiltering(
            includes = filters.filter { state(it) == INCLUDE }.map(key),
            excludes = filters.filter { state(it) == EXCLUDE }.map(key),
            list = list,
            transform = transform,
            transformIncludes = transformIncludes,
            mustContainAll = mustContainAll,
        )

        fun <Base, Comparison> applyFiltering(
            filters: List<FilterEntry<Comparison>>,
            list: List<Base>,
            transform: (Base) -> List<Comparison>,
            transformIncludes: ((Base) -> List<Comparison>)? = null,
            mustContainAll: Boolean = true,
        ) = applyFiltering(
            includes = filters.filter { it.state == INCLUDE }.map { it.value },
            excludes = filters.filter { it.state == EXCLUDE }.map { it.value },
            list = list,
            transform = transform,
            transformIncludes = transformIncludes,
            mustContainAll = mustContainAll,
        )

        fun <Base, Comparison> applyFiltering(
            includes: Collection<Comparison>,
            excludes: Collection<Comparison>,
            list: List<Base>,
            transform: (Base) -> List<Comparison>,
            transformIncludes: ((Base) -> List<Comparison>)? = null,
            mustContainAll: Boolean = true,
        ): List<Base> {
            if (includes.isEmpty() && excludes.isEmpty()) return list

            return list.filter {
                val target = transform(it)

                if (excludes.isNotEmpty() && !target.none(excludes::contains)) return@filter false

                if (includes.isEmpty()) return@filter true
                val targetIncludes = transformIncludes?.invoke(it) ?: target
                return@filter if (mustContainAll) {
                    targetIncludes.containsAll(includes)
                } else {
                    includes.any { targetIncludes.contains(it) }
                }
            }
        }
    }

    fun next(): FilterIncludeExcludeState {
        val values = values()
        return values[(values.indexOf(this) + 1) % values.size]
    }
}
