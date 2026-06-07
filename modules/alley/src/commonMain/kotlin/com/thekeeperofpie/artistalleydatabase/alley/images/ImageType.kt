package com.thekeeperofpie.artistalleydatabase.alley.images

/**
 * Image cache is tracked via a [typeName] + [version] combination, which is changed each time the
 * image semantically changes (like for a resolution bump), which will invalidate the entry in the
 * database. This was done instead of a version in the schema to avoid unnecessary user database
 * upgrades, but this not exclude that from happening in the future.
 */
enum class ImageType(private val typeName: String, private val version: Int) {
    ANILIST(typeName = "aniList", version = 2),
    OPEN_LIBRARY(typeName = "openLibrary", version = 1),
    STEAM(typeName = "steam", version = 2),
    TMDB(typeName = "tmdb", version = 1),
    WIKIPEDIA(typeName = "wikipedia", version = 2),
    ;

    internal val serializedName = "$typeName-$version"

    companion object {
        fun fromSerializedName(serializedName: String) =
            entries.find { it.serializedName == serializedName }
    }
}
