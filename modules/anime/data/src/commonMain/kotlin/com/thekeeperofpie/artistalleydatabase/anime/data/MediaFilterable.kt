package com.thekeeperofpie.artistalleydatabase.anime.data

import kotlinx.serialization.Serializable

interface MediaFilterable {
    val mediaId: String
    val isAdult: Boolean?
    val mediaListStatus : MediaListStatus?
    val progress: Int?
    val progressVolumes: Int?
    val scoreRaw: Double?
    val ignored: Boolean
    val showLessImportantTags: Boolean
    val showSpoilerTags: Boolean
}

@Serializable
data class MediaFilterableData(
    override val mediaId: String,
    override val isAdult: Boolean?,
    override val mediaListStatus: MediaListStatus?,
    override val progress: Int?,
    override val progressVolumes: Int?,
    override val scoreRaw: Double?,
    override val ignored: Boolean,
    override val showLessImportantTags: Boolean,
    override val showSpoilerTags: Boolean
) : MediaFilterable
