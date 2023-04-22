package com.thekeeperofpie.artistalleydatabase.animethemes

import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.NetworkSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AnimeThemesHiltModule {

    @Singleton
    @Provides
    fun provideAnimeThemesApi(appJson: AppJson, networkSettings: NetworkSettings) =
        AnimeThemesApi(appJson, networkSettings)
}
