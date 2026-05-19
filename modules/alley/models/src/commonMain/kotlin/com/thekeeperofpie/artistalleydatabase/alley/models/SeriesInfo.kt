package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TmdbType
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class SeriesInfo(
    val id: String,
    val uuid: Uuid,
    val notes: String?,
    val aniListId: Long?,
    val aniListType: AniListType,
    val wikipediaId: Long?,
    val tmdbId: String?,
    val tmdbType: TmdbType?,
    val steamId: String?,
    val openLibraryId: String?,
    val source: SeriesSource,
    val titlePreferred: String,
    val titleEnglish: String,
    val titleRomaji: String,
    val titleNative: String,
    val synonyms: List<String>,
    val link: String?,
    val faked: Boolean = false,
) {
    val resolvedLink = when {
        link != null -> link
        tmdbId != null -> when (tmdbType) {
            TmdbType.NONE -> null
            TmdbType.TV -> "https://themoviedb.org/tv/$tmdbId"
            TmdbType.MOVIE -> "https://themoviedb.org/movie/$tmdbId"
            null -> null
        }
        wikipediaId != null -> "https://en.wikipedia.org/?curid=$wikipediaId"
        steamId != null -> "https://store.steampowered.com/app/$steamId"
        openLibraryId != null -> "https://openlibrary.org/books/$openLibraryId"
        else -> null
    }

    companion object {
        fun fake(id: String) = SeriesInfo(
            id = id,
            uuid = Utils.uuidFromRandomBytes(id),
            notes = null,
            aniListId = null,
            aniListType = AniListType.NONE,
            wikipediaId = null,
            tmdbId = null,
            tmdbType = null,
            steamId = null,
            openLibraryId = null,
            source = SeriesSource.NONE,
            titlePreferred = id,
            titleEnglish = id,
            titleRomaji = id,
            titleNative = id,
            synonyms = emptyList(),
            link = null,
            faked = true,
        )
    }
}
