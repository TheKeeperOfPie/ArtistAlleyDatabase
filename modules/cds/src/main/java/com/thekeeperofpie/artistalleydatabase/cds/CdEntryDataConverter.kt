package com.thekeeperofpie.artistalleydatabase.cds

import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.form.EntrySection.MultiText.Entry.Different.serializedValue
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
            .encodeToString(AlbumColumnEntry(album.id, album.title))

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

    fun titleEntry(album: AlbumEntry) = EntrySection.MultiText.Entry.Prefilled(
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

    private val AlbumEntry.title get() = names["ja-latn"] ?: names["en"] ?: names["jp"] ?: ""
}