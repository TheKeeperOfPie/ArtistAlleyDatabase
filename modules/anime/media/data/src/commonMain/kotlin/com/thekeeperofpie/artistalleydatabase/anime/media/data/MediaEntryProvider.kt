package com.thekeeperofpie.artistalleydatabase.anime.media.data

interface MediaEntryProvider<Media, MediaEntry> {
    /** Proxies to a real type to decouple the media data class from recommendations */
    fun mediaEntry(media: Media): MediaEntry
    fun mediaFilterable(entry: MediaEntry): MediaFilterableData
    fun media(entry: MediaEntry): Media
    fun copyMediaEntry(entry: MediaEntry, data: MediaFilterableData): MediaEntry
    fun id(entry: MediaEntry): String
}
