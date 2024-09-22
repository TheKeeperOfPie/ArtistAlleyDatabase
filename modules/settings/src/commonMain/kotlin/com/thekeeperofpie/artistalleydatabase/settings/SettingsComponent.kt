package com.thekeeperofpie.artistalleydatabase.settings

import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.news.NewsSettings
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import me.tatarka.inject.annotations.Provides

interface SettingsComponent {

    val settingsViewModel: () -> SettingsViewModel

    @Provides
    fun provideArtSettings(settingsProvider: SettingsProvider) = settingsProvider as ArtSettings

    @Provides
    fun provideCropSettings(settingsProvider: SettingsProvider) = settingsProvider as CropSettings

    @Provides
    fun provideNetworkSettings(settingsProvider: SettingsProvider) =
        settingsProvider as NetworkSettings

    @Provides
    fun provideAnimeSettings(settingsProvider: SettingsProvider) =
        settingsProvider as AnimeSettings

    @Provides
    fun provideMonetizationSettings(settingsProvider: SettingsProvider) =
        settingsProvider as MonetizationSettings

    @Provides
    fun provideNewsSettings(settingsProvider: SettingsProvider) = settingsProvider as NewsSettings

    @Provides
    fun provideAniListSettings(settingsProvider: SettingsProvider) =
        settingsProvider as AniListSettings
}
