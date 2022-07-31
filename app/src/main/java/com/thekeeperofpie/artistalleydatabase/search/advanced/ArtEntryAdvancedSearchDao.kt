package com.thekeeperofpie.artistalleydatabase.search.advanced

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.sqlite.db.SimpleSQLiteQuery
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao.Companion.toBit
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao.Companion.wrapMatchQuery
import com.thekeeperofpie.artistalleydatabase.art.SourceType

@Dao
interface ArtEntryAdvancedSearchDao : ArtEntryDao {

    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")
    }

    fun search(query: AdvancedSearchQuery): PagingSource<Int, ArtEntry> {
        val queryPieces = mutableListOf<String>()

        queryPieces += query.artists.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "artists:${wrapMatchQuery(it)}" }
        queryPieces += query.series.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "seriesSearchable:${wrapMatchQuery(it)}" }
        queryPieces += query.seriesById.map { "series:$it" }
        queryPieces += query.characters.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "charactersSearchable:${wrapMatchQuery(it)}" }
        queryPieces += query.charactersById.map { "characters:$it" }
        queryPieces += query.tags.flatMap { it.split(WHITESPACE_REGEX) }
            .map { "tags:${wrapMatchQuery(it)}" }
        query.notes.takeUnless(String?::isNullOrBlank)?.let {
            queryPieces += it.split(WHITESPACE_REGEX).map { "notes:${wrapMatchQuery(it)}" }
        }
        when (val source = query.source) {
            is SourceType.Convention -> {
                queryPieces += "sourceType:${source.serializedType}"
                queryPieces += source.name.takeIf(String::isNotBlank)
                    ?.split(WHITESPACE_REGEX)
                    ?.map { "sourceValue:${wrapMatchQuery(it)}" }
                    .orEmpty()
                source.year
                    ?.let { "sourceValue:${wrapMatchQuery(it.toString())}" }
                    ?.let { queryPieces += it }
                queryPieces += source.hall.takeIf(String::isNotBlank)
                    ?.split(WHITESPACE_REGEX)
                    ?.map { "sourceValue:${wrapMatchQuery(it)}" }
                    .orEmpty()
                queryPieces += source.booth.takeIf(String::isNotBlank)
                    ?.split(WHITESPACE_REGEX)
                    ?.map { "sourceValue:${wrapMatchQuery(it)}" }
                    .orEmpty()
            }
            is SourceType.Custom -> {
                queryPieces += "sourceType:${source.serializedType}"
                queryPieces += source.value.takeIf(String::isNotBlank)
                    ?.split(WHITESPACE_REGEX)
                    ?.map { "sourceValue:${wrapMatchQuery(it)}" }
                    .orEmpty()
            }
            is SourceType.Online -> TODO()
            SourceType.Different,
            SourceType.Unknown,
            null -> {
                // Do nothing
            }
        }

        query.artistsLocked?.let { queryPieces += "artistsLocked:${it.toBit()}" }
        query.seriesLocked?.let { queryPieces += "seriesLocked:${it.toBit()}" }
        query.charactersLocked?.let { queryPieces += "charactersLocked:${it.toBit()}" }
        query.sourceLocked?.let { queryPieces += "sourceLocked:${it.toBit()}" }
        query.tagsLocked?.let { queryPieces += "tagsLocked:${it.toBit()}" }
        query.notesLocked?.let { queryPieces += "notesLocked:${it.toBit()}" }
        query.printSizeLocked?.let { queryPieces += "printSizeLocked:${it.toBit()}" }

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
        } + "\nORDER BY art_entries.lastEditTime DESC"

        return getEntries(SimpleSQLiteQuery(statement, queryPieces.toTypedArray()))
    }
}