package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.form.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistEntry
import kotlinx.serialization.encodeToString
import javax.inject.Inject

class VgmdbDataConverter @Inject constructor(
    private val vgmdbJson: VgmdbJson,
) {

    fun catalogIdPlaceholder(
        album: SearchResults.AlbumResult
    ): Entry.Prefilled<SearchResults.AlbumResult> {
        val catalogId = album.catalogId
        val titleText: String
        val subtitleText: String?
        if (catalogId == null) {
            titleText = album.name
            subtitleText = null
        } else {
            titleText = catalogId
            subtitleText = album.name
        }

        val serializedValue = vgmdbJson.json
            .encodeToString(AlbumColumnEntry(album.id, album.catalogId, titleText))

        return Entry.Prefilled(
            value = album,
            id = albumEntryId(album.id),
            text = titleText,
            titleText = titleText,
            subtitleText = subtitleText,
            imageLink = "https://vgmdb.net/album/${album.id}",
            serializedValue = serializedValue,
            searchableValue = album.names.values
                .filterNot(String?::isNullOrBlank)
                .joinToString { it.trim() }
        )
    }

    fun catalogEntry(album: AlbumEntry): Entry.Prefilled<AlbumEntry> {
        val catalogId = album.catalogId
        val titleText: String
        val subtitleText: String?
        if (catalogId == null) {
            titleText = album.title
            subtitleText = null
        } else {
            titleText = catalogId
            subtitleText = album.title
        }

        val serializedValue = vgmdbJson.json
            .encodeToString(AlbumColumnEntry(album.id, album.catalogId, album.title))

        return Entry.Prefilled(
            value = album,
            id = albumEntryId(album.id),
            text = titleText,
            subtitleText = subtitleText,
            image = album.coverThumb,
            imageLink = "https://vgmdb.net/album/${album.id}",
            serializedValue = serializedValue,
            searchableValue = album.names.values
                .filterNot(String?::isNullOrBlank)
                .joinToString { it.trim() }
        )
    }

    fun catalogEntry(album: AlbumColumnEntry): Entry {
        val catalogId = album.catalogId
        val titleText: String
        val subtitleText: String?
        if (catalogId == null) {
            titleText = album.title
            subtitleText = null
        } else {
            titleText = catalogId
            subtitleText = album.title
        }

        val serializedValue = vgmdbJson.json
            .encodeToString(AlbumColumnEntry(album.id, album.catalogId, album.title))

        return Entry.Prefilled(
            value = album,
            id = albumEntryId(album.id),
            text = titleText,
            subtitleText = subtitleText,
            imageLink = "https://vgmdb.net/album/${album.id}",
            serializedValue = serializedValue,
            searchableValue = album.title,
        )
    }

    fun titleEntry(album: AlbumEntry): Entry.Prefilled<AlbumEntry> {
        val serializedValue = vgmdbJson.json
            .encodeToString(AlbumColumnEntry(album.id, album.catalogId, album.title))

        return Entry.Prefilled(
            value = album,
            id = albumEntryId(album.id),
            text = album.title,
            image = album.coverThumb,
            imageLink = "https://vgmdb.net/album/${album.id}",
            serializedValue = serializedValue,
            searchableValue = album.names.values
                .filterNot(String?::isNullOrBlank)
                .joinToString { it.trim() }
        )
    }

    fun titleEntry(album: AlbumColumnEntry): Entry {
        val serializedValue = vgmdbJson.json
            .encodeToString(AlbumColumnEntry(album.id, album.catalogId, album.title))

        return Entry.Prefilled(
            value = album,
            id = albumEntryId(album.id),
            text = album.title,
            imageLink = "https://vgmdb.net/album/${album.id}",
            serializedValue = serializedValue,
            searchableValue = album.title,
        )
    }

    fun databaseToCatalogIdEntry(value: String?) =
        when (val either = vgmdbJson.parseCatalogIdColumn(value)) {
            is Either.Right -> catalogEntry(either.value)
            is Either.Left -> Entry.Custom(either.value)
        }

    fun databaseToTitleEntry(value: String?) =
        when (val either = vgmdbJson.parseTitleColumn(value)) {
            is Either.Right -> titleEntry(either.value)
            is Either.Left -> Entry.Custom(either.value)
        }

    fun databaseToArtistEntry(value: String) =
        when (val either = vgmdbJson.parseArtistColumn(value)) {
            is Either.Right -> artistPlaceholder(either.value)
            is Either.Left -> Entry.Custom(either.value)
        }

    fun databaseToDiscEntry(value: String) = vgmdbJson.parseDiscColumn(value)

    fun artistPlaceholder(
        artist: SearchResults.ArtistResult
    ): Entry.Prefilled<SearchResults.ArtistResult> {
        val serializedValue =
            vgmdbJson.json.encodeToString(
                ArtistColumnEntry(
                    artist.id,
                    mapOf("en" to artist.name),
                    manuallyChosen = true
                )
            )
        return Entry.Prefilled(
            value = artist,
            id = artistEntryId(artist),
            text = artist.name,
            imageLink = "https://vgmdb.net/artist/${artist.id}",
            serializedValue = serializedValue,
            searchableValue = artist.name
        )
    }

    fun artistPlaceholder(
        artist: ArtistColumnEntry
    ): Entry.Prefilled<ArtistColumnEntry> {
        val serializedValue = vgmdbJson.json.encodeToString(artist)
        return Entry.Prefilled(
            value = artist,
            id = artistEntryId(artist),
            text = artist.name,
            imageLink = artist.id?.let { "https://vgmdb.net/artist/${it}" },
            serializedValue = serializedValue,
            searchableValue = artist.names.values
                .filterNot(String?::isNullOrBlank)
                .joinToString { it.trim() }
        )
    }

    fun artistEntry(
        artist: ArtistEntry,
        manualChoice: Boolean = false,
    ): Entry.Prefilled<ArtistEntry> {
        val serializedValue = vgmdbJson.json
            .encodeToString(ArtistColumnEntry(artist.id, artist.names, manualChoice))
        return Entry.Prefilled(
            value = artist,
            id = artistEntryId(artist),
            text = artist.name,
            image = artist.pictureThumb,
            imageLink = "https://vgmdb.net/artist/${artist.id}",
            serializedValue = serializedValue,
            searchableValue = artist.names.values
                .filterNot(String?::isNullOrBlank)
                .joinToString { it.trim() }
        )
    }

    fun artistColumnData(entry: Entry) = when ((entry as? Entry.Prefilled<*>)?.value) {
        is ArtistEntry, is ArtistColumnEntry -> {
            vgmdbJson.parseArtistColumn(entry.serializedValue).rightOrNull()
        }
        else -> null
    }

    fun discEntries(album: AlbumEntry) = album.discs.mapNotNull(vgmdbJson::parseDiscColumn)

    private fun artistEntryId(artist: SearchResults.ArtistResult) = "vgmdbArtist_${artist.id}"
    private fun artistEntryId(artist: ArtistColumnEntry) = "vgmdbArtist_${artist.id ?: artist.name}"
    private fun artistEntryId(artist: ArtistEntry) = "vgmdbArtist_${artist.id}"

    private fun albumEntryId(id: String) = "vgmdbAlbum_$id"

    private val AlbumEntry.title get() = names["ja-latn"] ?: names["en"] ?: names["jp"] ?: ""

    private val SearchResults.AlbumResult.name
        get() = names["ja-latn"] ?: names["en"] ?: names["jp"] ?: ""

    private val ArtistColumnEntry.name
        get() = names["ja-latn"] ?: names["en"] ?: names["jp"] ?: names.values.firstOrNull() ?: ""

    private val ArtistEntry.name
        get() = names["ja-latn"] ?: names["en"] ?: names["jp"] ?: names.values.firstOrNull() ?: ""
}