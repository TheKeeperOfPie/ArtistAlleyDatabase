package com.thekeeperofpie.artistalleydatabase.search.advanced

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.sqlite.db.SimpleSQLiteQuery
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.SourceType

@Dao
interface ArtEntryAdvancedSearchDao : ArtEntryDao {

    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")
    }

    fun search(query: AdvancedSearchQuery): PagingSource<Int, ArtEntry> {
        val queryPieces = mutableListOf<String>()

        queryPieces += query.artists.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "artists:$it" }
        queryPieces += query.series.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "seriesSearchable:$it" }
        queryPieces += query.seriesById.map { "series:$it" }
        queryPieces += query.characters.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "charactersSearchable:*$it*" }
        queryPieces += query.charactersById.map { "characters:*$it*" }
        queryPieces += query.tags.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "tags:$it" }
        query.notes.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX).map { "notes:$it" }
        }
        when (val source = query.source) {
            is SourceType.Convention -> {
                queryPieces += "sourceType:${source.serializedType}"
                queryPieces += "sourceValue:${source.name}"
                queryPieces += "sourceValue:${source.year}"
                queryPieces += "sourceValue:${source.hall}"
                queryPieces += "sourceValue:${source.booth}"
            }
            is SourceType.Custom -> {
                queryPieces += "sourceType:${source.serializedType}"
                queryPieces += "sourceValue:${source.value}"
            }
            is SourceType.Online -> TODO()
            SourceType.Different,
            SourceType.Unknown,
            null -> {
                // Do nothing
            }
        }

        if (queryPieces.isEmpty()) {
            return getEntries()
        }

        val statement = queryPieces.joinToString("\nINTERSECT\n") {
            """
                SELECT *
                FROM art_entries
                JOIN art_entries_fts ON art_entries.id = art_entries_fts.id
                WHERE art_entries_fts MATCH ?
                """.trimIndent()
        }

        return getEntries(SimpleSQLiteQuery(statement, queryPieces.toTypedArray()))
    }
}