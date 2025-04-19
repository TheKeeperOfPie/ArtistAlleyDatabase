package com.thekeeperofpie.artistalleydatabase.anime.media.data

import com.anilist.data.MediaTagsQuery
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagEntry

interface MediaTagEntry {

    val name: String

    data class Category(
        override val name: String,
        override val children: Map<String, TagEntry>,
        val expanded: Boolean = false,
        val hasAnySelected: Boolean = false,
    ) : MediaTagEntry, TagEntry.Category {
        override val id = name

        override fun copyWithChildren(children: Map<String, TagEntry>) = copy(children = children)

        override fun matches(query: String) = name.contains(query, ignoreCase = true)

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
        override val id: String,
        override val name: String,
        val category: String?,
        val description: String?,
        val isAdult: Boolean?,
        override val value: MediaTagsQuery.Data.MediaTagCollection,
        override val state: FilterIncludeExcludeState = FilterIncludeExcludeState.DEFAULT,
        override val clickable: Boolean = true,
    ) : FilterEntry<MediaTagsQuery.Data.MediaTagCollection>, MediaTagEntry, TagEntry.Tag {
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

        override fun matches(query: String) = name.contains(query, ignoreCase = true)
    }
}
