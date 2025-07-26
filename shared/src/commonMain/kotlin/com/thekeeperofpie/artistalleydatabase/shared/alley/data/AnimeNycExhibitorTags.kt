package com.thekeeperofpie.artistalleydatabase.shared.alley.data

object AnimeNycExhibitorTags {
    val TAGS = setOf(
        "Apparel and Fashion",
        "BIPOC Owned",
        "Books  (non-Manga)",
        "Cat Person",
        "Charms",
        "Collectibles",
        "Conventions",
        "Commissions",
        "Cosplay",
        "Education",
        "Exclusives",
        "First Time Artist",
        "First Time Exhibitor",
        "Food / Drink",
        "Japanese Culture",
        "Keychains",
        "LGBT+ Owned",
        "Manga",
        "NYC Local",
        "Other",
        "Prints",
        "Tabletop Games",
        "Toys",
        "Video Games",
        "Webcomics",
        "Woman Owned",
        "Zines",
    )

    fun parseFlags(tags: Collection<String>) = tags.fold(0L) { flags, tag ->
        val index = TAGS.indexOf(tag) + 1
        if (index < 1) {
            throw IllegalStateException("Bad tag $tag")
        }
        flags or (1L shl index)
    }
}
