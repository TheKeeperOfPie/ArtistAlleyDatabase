package com.thekeeperofpie.artistalleydatabase.anime.studios.data

interface StudioEntryProvider<Studio, StudioEntry, MediaEntry> {
    /** Proxies to a real type to decouple the studio data class */
    fun studioEntry(studio: Studio, media: List<MediaEntry>): StudioEntry
    fun copyStudioEntry(entry: StudioEntry, media: List<MediaEntry>): StudioEntry
    fun media(entry: StudioEntry): List<MediaEntry>
    fun id(entry: StudioEntry): String
}
