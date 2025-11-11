package com.thekeeperofpie.artistalleydatabase.animethemes

import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongsProvider
import dev.zacsweers.metro.Binds

interface AnimeThemesComponent {

    @Binds
    val AnimeThemesSongsProvider.bindAnimeSongsProvider: AnimeSongsProvider?
}
