package com.thekeeperofpie.artistalleydatabase.cds

import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
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

    fun titleEntry(album: AlbumEntry): EntrySection.MultiText.Entry {
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

    private val AlbumEntry.title get() = names["ja-latn"] ?: names["en"] ?: names["jp"] ?: ""
}