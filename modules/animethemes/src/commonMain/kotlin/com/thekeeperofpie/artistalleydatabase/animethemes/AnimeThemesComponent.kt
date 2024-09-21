package com.thekeeperofpie.artistalleydatabase.animethemes

import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongsProvider
import me.tatarka.inject.annotations.Provides

interface AnimeThemesComponent {

    val AnimeThemesSongsProvider.bind: AnimeSongsProvider?
        @Provides get() = this
}
