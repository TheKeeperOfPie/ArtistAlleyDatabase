package com.thekeeperofpie.artistalleydatabase.settings

import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.news.NewsSettings
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsHiltModule {

    @Singleton
    @Provides
    fun provideArtSettings(settingsProvider: SettingsProvider) = settingsProvider as ArtSettings

    @Singleton
    @Provides
    fun provideCropSettings(settingsProvider: SettingsProvider) = settingsProvider as CropSettings

    @Singleton
    @Provides
    fun provideNetworkSettings(settingsProvider: SettingsProvider) =
        settingsProvider as NetworkSettings

    @Singleton
    @Provides
    fun provideAnimeSettings(settingsProvider: SettingsProvider) =
        settingsProvider as AnimeSettings

    @Singleton
    @Provides
    fun provideMonetizationSettings(settingsProvider: SettingsProvider) =
        settingsProvider as MonetizationSettings

    @Singleton
    @Provides
    fun provideNewsSettings(settingsProvider: SettingsProvider) = settingsProvider as NewsSettings

    @Singleton
    @Provides
    fun provideAniListSettings(settingsProvider: SettingsProvider): AniListSettings =
        settingsProvider
}
