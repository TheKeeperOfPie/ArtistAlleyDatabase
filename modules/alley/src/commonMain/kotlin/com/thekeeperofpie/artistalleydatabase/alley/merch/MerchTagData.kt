package com.thekeeperofpie.artistalleydatabase.alley.merch

import com.thekeeperofpie.artistalleydatabase.alley.data.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.AlleyTagEntry

data class MerchTagData(val merchEntries: List<MerchEntry>) {
    val tags: List<Pair<String, AlleyTagEntry.Category>>
    val categoryToTagsMap: Map<String, List<String>>

    init {
        // Categories are decided by data entry
        val merchToCategories = merchEntries.map {
            it to it.categories?.split(",")?.map(String::trim).orEmpty()
        }

        val other = "Other" to AlleyTagEntry.Category(
            id = "Other",
            children = merchEntries.filter { it.categories.isNullOrEmpty() }
                .sortedBy { it.name }
                .associate { it.name to AlleyTagEntry.Tag(it.name) }
        )
        val categories = merchToCategories.flatMap { it.second }.distinct().sorted()
            .filter { it.isNotEmpty() }
        val categoryToTags = categories.map { category ->
            category to merchToCategories.filter { it.second.contains(category) }
                .map { it.first.name }
        }
        categoryToTagsMap = categoryToTags.associate { it }
        tags = categoryToTags.map { (category, tags) ->
            val tagsForCategory = merchToCategories.filter { it.second.contains(category) }
                .map { it.first }
                .sortedBy { it.name }
            val allId = "all$category" // TODO: Find a better model
            category to AlleyTagEntry.Category(
                id = category,
                children = mapOf(allId to AlleyTagEntry.Tag(allId)) +
                        tagsForCategory.associate {
                            it.name to AlleyTagEntry.Tag(it.name)
                        },
            )
        } + other
    }

    fun selected(merchIdIn: Set<String>, merchId: String): Boolean {
        if (merchId.startsWith("all")) {
            return merchIdIn.containsAll(categoryToTagsMap[merchId.removePrefix("all")].orEmpty())
        }

        return merchId in merchIdIn
    }

    fun toggle(
        merchIdsLockedIn: Set<String>,
        merchIdIn: Set<String>,
        merchId: String,
        wasSelected: Boolean,
    ): Set<String> {
        if (merchId.startsWith("all")) {
            val tags = categoryToTagsMap[merchId.removePrefix("all")].orEmpty()
            return if (wasSelected) {
                merchIdIn - tags
            } else {
                merchIdIn + tags
            }
        }

        if (merchId !in merchIdsLockedIn) {
            return if (merchIdIn.contains(merchId)) {
                merchIdIn - merchId
            } else {
                merchIdIn + merchId
            }
        }

        return merchIdIn
    }
}
