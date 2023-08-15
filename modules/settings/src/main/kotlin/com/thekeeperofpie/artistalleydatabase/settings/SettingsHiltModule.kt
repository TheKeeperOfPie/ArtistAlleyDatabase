package com.thekeeperofpie.artistalleydatabase.settings

import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.entry.EntrySettings
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
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
    fun provideEntrySettings(settingsProvider: SettingsProvider) = settingsProvider as EntrySettings

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
    fun provideAniListSettings(settingsProvider: SettingsProvider) =
        settingsProvider as AniListSettings
}
