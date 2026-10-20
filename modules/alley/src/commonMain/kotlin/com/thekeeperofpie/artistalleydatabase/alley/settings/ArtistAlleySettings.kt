package com.thekeeperofpie.artistalleydatabase.alley.settings

import com.thekeeperofpie.artistalleydatabase.alley.AlleyRootDestination
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterSettings
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortSettings
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchDisplayType
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterSettings
import com.thekeeperofpie.artistalleydatabase.alley.ui.DisplaySettings
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting

interface ArtistAlleySettings : ArtistSortFilterSettings, StampRallySortSettings, DisplaySettings,
    SeriesSortFilterSettings {

    var appTheme: AppThemeSetting
    var displayType: SearchDisplayType

    var dataYear: DataYear
    var languageOption: AniListLanguageOption
    var easterEggEnabled: Boolean

    var rootDestination: AlleyRootDestination
}
