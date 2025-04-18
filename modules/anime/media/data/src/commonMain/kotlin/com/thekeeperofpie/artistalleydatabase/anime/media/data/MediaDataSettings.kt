package com.thekeeperofpie.artistalleydatabase.anime.media.data

import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Very poorly named, but this holds user settings required to use [MediaFilteringData] to filter
 * media entries. This is not well isolated and can probably be improved.
 */
interface MediaDataSettings {
    val showAdult: MutableStateFlow<Boolean>
    val showIgnored: StateFlow<Boolean>
    val showLessImportantTags: MutableStateFlow<Boolean>
    val showSpoilerTags: MutableStateFlow<Boolean>
    val mediaIgnoreHide: MutableStateFlow<Boolean>
    val collapseAnimeFiltersOnClose: MutableStateFlow<Boolean>
    val languageOptionMedia: MutableStateFlow<AniListLanguageOption>
    val preferredMediaType: MutableStateFlow<MediaType>
    val mediaViewOption: MutableStateFlow<MediaViewOption>
}
