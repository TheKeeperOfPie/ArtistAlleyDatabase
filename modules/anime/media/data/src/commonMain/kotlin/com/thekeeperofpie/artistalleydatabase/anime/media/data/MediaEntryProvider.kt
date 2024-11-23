package com.thekeeperofpie.artistalleydatabase.anime.media.data

interface MediaEntryProvider<Media, MediaEntry> {
    /** Proxies to a real type to decouple the media data class from recommendations */
    fun mediaEntry(media: Media): MediaEntry
    fun mediaFilterable(entry: MediaEntry): MediaFilterableData
    fun copyMediaEntry(entry: MediaEntry, data: MediaFilterableData): MediaEntry
}
