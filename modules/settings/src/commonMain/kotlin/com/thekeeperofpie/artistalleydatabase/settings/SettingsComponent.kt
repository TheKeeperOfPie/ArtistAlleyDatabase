package com.thekeeperofpie.artistalleydatabase.settings

import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.news.NewsSettings
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffSettings
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import me.tatarka.inject.annotations.Provides

interface SettingsComponent {

    val settingsViewModel: () -> SettingsViewModel

    val SettingsProvider.bindAnimeSettings: AnimeSettings
        @Provides get() = this

    val SettingsProvider.bindAniListSettings: AniListSettings
        @Provides get() = this

    val SettingsProvider.bindArtSettings: ArtSettings
        @Provides get() = this

    val SettingsProvider.bindCharacterSettings: CharacterSettings
        @Provides get() = this

    val SettingsProvider.bindCropSettings: CropSettings
        @Provides get() = this

    val SettingsProvider.bindIgnoreSettings: IgnoreSettings
        @Provides get() = this

    val SettingsProvider.bindMediaDataSettings: MediaDataSettings
        @Provides get() = this

    val SettingsProvider.bindMonetizationSettings: MonetizationSettings
        @Provides get() = this

    val SettingsProvider.bindNetworkSettings: NetworkSettings
        @Provides get() = this

    val SettingsProvider.bindNewsSettings: NewsSettings
        @Provides get() = this

    val SettingsProvider.bindStaffSettings: StaffSettings
        @Provides get() = this
}
