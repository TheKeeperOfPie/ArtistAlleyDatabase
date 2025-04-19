package com.thekeeperofpie.artistalleydatabase.alley.tags

import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagEntry

interface AlleyTagEntry {

    data class Tag(override val id: String) : TagEntry.Tag {
        override fun matches(query: String) = id.contains(query, ignoreCase = true)
    }

    data class Category(
        override val id: String,
        override val children: Map<String, TagEntry>,
    ) : TagEntry.Category {
        override fun copyWithChildren(children: Map<String, TagEntry>) = copy(children = children)
        override fun matches(query: String) = id.contains(query, ignoreCase = true)
    }
}
