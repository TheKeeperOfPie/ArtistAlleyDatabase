package com.thekeeperofpie.artistalleydatabase.anime.utils

enum class IncludeExcludeState {
    DEFAULT, INCLUDE, EXCLUDE;

    companion object {

        fun <Base, StateHolder, Comparison> applyFiltering(
            filters: List<StateHolder>,
            list: List<Base>,
            state: (StateHolder) -> IncludeExcludeState,
            key: (StateHolder) -> Comparison,
            transform: (Base) -> List<Comparison>,
            transformIncludes: ((Base) -> List<Comparison>)? = null,
        ): List<Base> {
            val includes = filters.filter { state(it) == INCLUDE }.map(key)
            val excludes = filters.filter { state(it) == EXCLUDE }.map(key)
            if (includes.isEmpty() && excludes.isEmpty()) return list

            return list.filter {
                val target = transform(it)

                val passExcludes = excludes.isEmpty() || target.none(excludes::contains)
                if (!passExcludes) return@filter false

                val targetIncludes = transformIncludes?.invoke(it) ?: target
                includes.isEmpty() || targetIncludes.containsAll(includes)
            }
        }
    }

    fun next(): IncludeExcludeState {
        val values = values()
        return values[(values.indexOf(this) + 1) % values.size]
    }
}