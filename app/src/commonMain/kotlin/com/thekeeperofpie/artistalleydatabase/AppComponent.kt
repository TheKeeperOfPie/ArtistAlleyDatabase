package com.thekeeperofpie.artistalleydatabase

import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterSettings
import com.thekeeperofpie.artistalleydatabase.anime.history.HistorySettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.news.NewsSettings
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffSettings
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.utils_compose.FullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import dev.zacsweers.metro.Provides

interface AppComponent {
    val settings: AnimeSettings
    val mediaGenreDialogController: MediaGenreDialogController
    val mediaTagDialogController: MediaTagDialogController
    val ignoreController: IgnoreController
    val markdown: Markdown
    val fullscreenImageHandler: FullscreenImageHandler

    @Provides
    fun bindAniListSettings(appSettings: AppSettings): AniListSettings = appSettings

    @Provides
    fun bindAnimeSettings(appSettings: AppSettings): AnimeSettings = appSettings

    @Provides
    fun bindArtSettings(appSettings: AppSettings): ArtSettings = appSettings

    @Provides
    fun bindCharacterSettings(appSettings: AppSettings): CharacterSettings = appSettings

    @Provides
    fun bindCropSettings(appSettings: AppSettings): CropSettings = appSettings

    @Provides
    fun bindHistorySettings(appSettings: AppSettings): HistorySettings = appSettings

    @Provides
    fun bindIgnoreSettings(appSettings: AppSettings): IgnoreSettings = appSettings

    @Provides
    fun bindMediaDataSettings(appSettings: AppSettings): MediaDataSettings = appSettings

    @Provides
    fun bindMonetizationSettings(appSettings: AppSettings): MonetizationSettings = appSettings

    @Provides
    fun bindNewsSettings(appSettings: AppSettings): NewsSettings = appSettings

    @Provides
    fun bindNetworkSettings(appSettings: AppSettings): NetworkSettings = appSettings

    @Provides
    fun bindStaffSettings(appSettings: AppSettings): StaffSettings = appSettings
}
