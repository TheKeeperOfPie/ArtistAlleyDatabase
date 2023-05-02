package com.thekeeperofpie.artistalleydatabase.anime

import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.chromium.net.CronetEngine
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnimeHiltModule {

    @Singleton
    @Provides
    fun provideAppMediaPlayer(scopedApplication: ScopedApplication, cronetEngine: CronetEngine) =
        AppMediaPlayer(scopedApplication, cronetEngine)

    @Singleton
    @Provides
    fun provideAnimeMediaIgnoreList(animeSettings: AnimeSettings) =
        AnimeMediaIgnoreList(animeSettings)
}
