package com.thekeeperofpie.artistalleydatabase.animethemes

import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongsProvider
import dev.zacsweers.metro.Provides

interface AnimeThemesComponent {

    @Provides
    fun bindAnimeSongsProvider(provider: AnimeThemesSongsProvider): AnimeSongsProvider? = provider
}
