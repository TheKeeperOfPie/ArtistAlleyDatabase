package com.thekeeperofpie.artistalleydatabase.cds

import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistEntry
import kotlinx.serialization.encodeToString
import javax.inject.Inject

class CdEntryDataConverter @Inject constructor(
    private val vgmdbJson: VgmdbJson,
) {

    fun catalogEntry(album: AlbumEntry): EntrySection.MultiText.Entry {
        val serializedValue = vgmdbJson.json
            .encodeToString(AlbumColumnEntry(album.id, album.catalogId, album.title))

        return EntrySection.MultiText.Entry.Prefilled(
            value = album,
            id = album.id,
            text = album.catalogId ?: album.title,
            image = album.coverThumb,
            imageLink = "https://vgmdb.net/album/${album.id}",
            serializedValue = serializedValue,
            searchableValue = album.names.values
                .filterNot(String?::isNullOrBlank)
                .joinToString { it.trim() }
        )
    }

    fun catalogEntry(album: AlbumColumnEntry): EntrySection.MultiText.Entry {
        val serializedValue = vgmdbJson.json
            .encodeToString(AlbumColumnEntry(album.id, album.catalogId, album.title))

        return EntrySection.MultiText.Entry.Prefilled(
            value = album,
            id = album.id,
            text = album.catalogId ?: album.title,
            imageLink = "https://vgmdb.net/album/${album.id}",
            serializedValue = serializedValue,
            searchableValue = album.title,
        )
    }

    fun titleEntry(album: AlbumEntry): EntrySection.MultiText.Entry.Prefilled<AlbumEntry> {
        val serializedValue = vgmdbJson.json
            .encodeToString(AlbumColumnEntry(album.id, album.catalogId, album.title))

        return EntrySection.MultiText.Entry.Prefilled(
            value = album,
            id = album.id,
            text = album.title,
            image = album.coverThumb,
            imageLink = "https://vgmdb.net/album/${album.id}",
            serializedValue = serializedValue,
            searchableValue = album.names.values
                .filterNot(String?::isNullOrBlank)
                .joinToString { it.trim() }
        )
    }

    fun titleEntry(album: AlbumColumnEntry): EntrySection.MultiText.Entry {
        val serializedValue = vgmdbJson.json
            .encodeToString(AlbumColumnEntry(album.id, album.catalogId, album.title))

        return EntrySection.MultiText.Entry.Prefilled(
            value = album,
            id = album.id,
            text = album.title,
            imageLink = "https://vgmdb.net/album/${album.id}",
            serializedValue = serializedValue,
            searchableValue = album.title,
        )
    }

    fun databaseToCatalogIdEntry(value: String?) =
        when (val either = vgmdbJson.parseCatalogIdColumn(value)) {
            is Either.Right -> catalogEntry(either.value)
            is Either.Left -> EntrySection.MultiText.Entry.Custom(either.value)
        }

    fun databaseToTitleEntry(value: String?) =
        when (val either = vgmdbJson.parseTitleColumn(value)) {
            is Either.Right -> titleEntry(either.value)
            is Either.Left -> EntrySection.MultiText.Entry.Custom(either.value)
        }


    fun vocalistEntry(artist: ArtistEntry) = artistEntry(artist)
    fun composerEntry(artist: ArtistEntry) = artistEntry(artist)

    private fun artistEntry(
        artist: ArtistEntry
    ): EntrySection.MultiText.Entry.Prefilled<ArtistEntry> {
        val serializedValue = vgmdbJson.json
            .encodeToString(ArtistColumnEntry(artist.id, artist.names))
        return EntrySection.MultiText.Entry.Prefilled(
            value = artist,
            id = artist.id,
            text = artist.name,
            image = artist.coverThumb,
            imageLink = "https://vgmdb.net/artist/${artist.id}",
            serializedValue = serializedValue,
            searchableValue = artist.names.values
                .filterNot(String?::isNullOrBlank)
                .joinToString { it.trim() }
        )
    }

    private val AlbumEntry.title get() = names["ja-latn"] ?: names["en"] ?: names["jp"] ?: ""

    private val ArtistEntry.name
        get() = names["ja-latn"] ?: names["en"] ?: names["jp"] ?: names.values.firstOrNull() ?: ""
}