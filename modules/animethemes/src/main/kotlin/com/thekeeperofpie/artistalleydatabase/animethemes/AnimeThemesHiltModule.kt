package com.thekeeperofpie.artistalleydatabase.animethemes

import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AnimeThemesHiltModule {

    @Singleton
    @Provides
    fun provideAnimeThemesApi(appJson: AppJson, okHttpClient: OkHttpClient) =
        AnimeThemesApi(appJson, okHttpClient)
}
