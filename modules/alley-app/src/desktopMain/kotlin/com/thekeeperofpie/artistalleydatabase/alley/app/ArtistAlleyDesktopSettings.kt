package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import kotlinx.coroutines.flow.MutableStateFlow
import me.tatarka.inject.annotations.Inject

@Inject
class ArtistAlleyDesktopSettings : AniListSettings, ArtistAlleySettings {
    override val aniListViewer = MutableStateFlow<AniListViewer?>(null)
    override val ignoreViewer = MutableStateFlow(false)
    override val lastKnownArtistsCsvSize = MutableStateFlow(-1L)
    override val lastKnownStampRalliesCsvSize = MutableStateFlow(-1L)
    override val displayType = MutableStateFlow("")
    override val artistsSortOption = MutableStateFlow("")
    override val artistsSortAscending = MutableStateFlow(true)
    override val stampRalliesSortOption = MutableStateFlow("")
    override val stampRalliesSortAscending = MutableStateFlow(true)
    override val showGridByDefault = MutableStateFlow(false)
    override val showRandomCatalogImage = MutableStateFlow(false)
    override val showOnlyConfirmedTags = MutableStateFlow(false)
    override val showOnlyFavorites = MutableStateFlow(false)
    override val forceOneDisplayColumn = MutableStateFlow(false)
}
