package com.thekeeperofpie.artistalleydatabase.anime

import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnimeHiltModule {

    @Singleton
    @Provides
    fun provideAppMediaPlayer(scopedApplication: ScopedApplication) =
        AppMediaPlayer(scopedApplication)
}
