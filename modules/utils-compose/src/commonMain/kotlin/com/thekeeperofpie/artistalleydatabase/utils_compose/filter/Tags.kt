package com.thekeeperofpie.artistalleydatabase.utils_compose.filter

sealed interface TagEntry {
    val id: String
    fun matches(query: String): Boolean

    interface Tag : TagEntry
    interface Category : TagEntry {
        val children: Map<String, TagEntry>
        fun copyWithChildren(children: Map<String, TagEntry>) : Category

        fun flatten(): List<Tag> = children.values.flatMap {
            when (it) {
                is Category -> it.flatten()
                is Tag -> listOf(it)
            }
        }
    }

    fun findTag(id: String): Tag? = when (this) {
        is Category -> {
            children.values.asSequence()
                .mapNotNull { it.findTag(id) }
                .firstOrNull()
        }
        is Tag -> takeIf { it.id == id }
    }

    fun filter(predicate: (Tag) -> Boolean): TagEntry? = when (this) {
        is Category -> {
            children.values
                .mapNotNull { it.filter(predicate) }
                .associateBy { it.id }
                .toList()
                .sortedWith { first, second ->
                    first.first.compareTo(second.first, ignoreCase = true)
                }
                .toMap()
                .takeIf { it.isNotEmpty() }
                ?.let { copyWithChildren(children = it) }
        }
        is Tag -> takeIf { predicate(it) }
    }

    fun replace(block: (Tag) -> Tag): TagEntry = when (this) {
        is Category -> {
            copyWithChildren(
                children = children.mapValues { (_, value) -> value.replace(block) }
                    .toList()
                    .sortedWith { first, second ->
                        first.first.compareTo(second.first, ignoreCase = true)
                    }
                    .toMap()
            )
        }
        is Tag -> block(this)
    }
}
