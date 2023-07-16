package com.thekeeperofpie.artistalleydatabase.compose.filter

import androidx.compose.ui.graphics.vector.ImageVector

interface FilterEntry<T> {
    val value: T
    val state: FilterIncludeExcludeState
    val leadingIconVector: ImageVector? get() = null
    val leadingIconContentDescription: Int? get() = null

    companion object {
        fun <T : Any?> values(
            values: Array<T>,
            included: Collection<T> = emptyList(),
            excluded: Collection<T> = emptyList()
        ) = values.map {
            FilterEntryImpl(
                it, FilterIncludeExcludeState.toState(
                    value = it,
                    included = included,
                    excluded = excluded
                )
            )
        }

        fun <T : Any?> values(
            values: Iterable<T>,
            included: Collection<T> = emptyList(),
            excluded: Collection<T> = emptyList()
        ) = values.map {
            FilterEntryImpl(
                it, FilterIncludeExcludeState.toState(
                    value = it,
                    included = included,
                    excluded = excluded
                )
            )
        }
    }

    data class FilterEntryImpl<T>(
        override val value: T,
        override val state: FilterIncludeExcludeState = FilterIncludeExcludeState.DEFAULT,
    ) : FilterEntry<T>
}
