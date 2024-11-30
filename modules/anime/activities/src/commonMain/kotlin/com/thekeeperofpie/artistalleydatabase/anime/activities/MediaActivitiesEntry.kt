package com.thekeeperofpie.artistalleydatabase.anime.activities

data class MediaActivitiesEntry(
    val following: List<MediaActivityEntry>,
    val global: List<MediaActivityEntry>,
)
