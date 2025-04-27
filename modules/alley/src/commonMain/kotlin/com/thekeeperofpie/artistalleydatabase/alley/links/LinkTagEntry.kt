package com.thekeeperofpie.artistalleydatabase.alley.links

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagEntry

interface LinkTagEntry {

    data class Tag(val type: Link.Type) : LinkTagEntry, TagEntry.Tag {
        override val id get() = type.name
        override fun matches(query: String) = false
    }

    data class Category(
        val category: LinkCategory,
        override val children: Map<String, TagEntry>,
    ) : LinkTagEntry, TagEntry.Category {
        override val id get() = category.name
        override fun copyWithChildren(children: Map<String, TagEntry>) = copy(children = children)
        override fun matches(query: String) = false
    }
}
