package com.thekeeperofpie.artistalleydatabase.anime.media.data

import com.anilist.data.MediaTagsQuery
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState

sealed interface MediaTagSection {
    // TODO: Convert to value classes
    val name: String

    fun findTag(id: String): Tag? = when (this) {
        is Category -> {
            children.values.asSequence()
                .mapNotNull { it.findTag(id) }
                .firstOrNull()
        }
        is Tag -> takeIf { it.id == id }
    }

    fun filter(predicate: (Tag) -> Boolean): MediaTagSection? = when (this) {
        is Category -> {
            children.values
                .mapNotNull { it.filter(predicate) }
                .associateBy { it.name }
                .toList()
                .sortedWith { first, second ->
                    first.first.compareTo(second.first, ignoreCase = true)
                }
                .toMap()
                .takeIf { it.isNotEmpty() }
                ?.let { copy(children = it) }
        }
        is Tag -> takeIf { predicate(it) }
    }

    fun replace(block: (Tag) -> Tag): MediaTagSection = when (this) {
        is Category -> {
            copy(
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

    data class Category(
        override val name: String,
        val children: Map<String, MediaTagSection>,
        val expanded: Boolean = false,
        val hasAnySelected: Boolean = false,
    ) : MediaTagSection {

        fun flatten(): List<Tag> = children.values.flatMap {
            when (it) {
                is Category -> it.flatten()
                is Tag -> listOf(it)
            }
        }

        class Builder(private val name: String) {
            private var children = mutableMapOf<String, Any>()

            fun getOrPutCategory(name: String): Builder {
                // Prefix to ensure tags don't conflict via name with actual child tags
                val key = "category_$name"
                return when (val existingSection = children[key]) {
                    is Builder -> existingSection
                    is Tag -> Builder(name)
                        .apply { children[key] = existingSection }
                        .also { children[key] = it }
                    else -> Builder(name)
                        .also { children[key] = it }
                }
            }

            fun addChild(it: MediaTagsQuery.Data.MediaTagCollection) {
                children[it.name] = Tag(
                    id = it.id.toString(),
                    name = it.name,
                    category = it.category,
                    isAdult = it.isAdult,
                    description = it.description,
                    value = it,
                )
            }

            fun build(): Category = Category(
                name = name,
                children = children.mapValues { (_, value) ->
                    when (value) {
                        is Builder -> value.build()
                        is Tag -> value
                        else -> throw IllegalStateException("Unexpected value $value")
                    }
                }.toList()
                    .sortedWith { first, second ->
                        first.first.compareTo(second.first, ignoreCase = true)
                    }
                    .toMap()
            )
        }
    }

    // TODO: Remove FilterEntry
    data class Tag(
        val id: String,
        override val name: String,
        val category: String?,
        val description: String?,
        val isAdult: Boolean?,
        override val value: MediaTagsQuery.Data.MediaTagCollection,
        override val state: FilterIncludeExcludeState = FilterIncludeExcludeState.DEFAULT,
        override val clickable: Boolean = true,
    ) : FilterEntry<MediaTagsQuery.Data.MediaTagCollection>, MediaTagSection {
        override val leadingIconVector = MediaDataUtils.tagLeadingIcon(
            isAdult = isAdult,
            isGeneralSpoiler = value.isGeneralSpoiler,
        )

        override val leadingIconContentDescription =
            MediaDataUtils.tagLeadingIconContentDescription(
                isAdult = isAdult,
                isGeneralSpoiler = value.isGeneralSpoiler,
            )

        constructor(tag: MediaTagsQuery.Data.MediaTagCollection) : this(
            id = tag.id.toString(),
            name = tag.name,
            category = tag.category,
            isAdult = tag.isAdult,
            description = tag.description,
            value = tag,
        )
    }
}
