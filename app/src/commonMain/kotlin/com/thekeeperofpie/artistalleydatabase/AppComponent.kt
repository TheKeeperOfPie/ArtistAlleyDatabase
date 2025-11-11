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
import dev.zacsweers.metro.Binds

interface AppComponent {
    val settings: AnimeSettings
    val mediaGenreDialogController: MediaGenreDialogController
    val mediaTagDialogController: MediaTagDialogController
    val ignoreController: IgnoreController
    val markdown: Markdown
    val fullscreenImageHandler: FullscreenImageHandler

    @Binds
    val AppSettings.bindAniListSettings: AniListSettings

    @Binds
    val AppSettings.bindAnimeSettings: AnimeSettings

    @Binds
    val AppSettings.bindArtSettings: ArtSettings

    @Binds
    val AppSettings.bindCharacterSettings: CharacterSettings

    @Binds
    val AppSettings.bindCropSettings: CropSettings

    @Binds
    val AppSettings.bindHistorySettings: HistorySettings

    @Binds
    val AppSettings.bindIgnoreSettings: IgnoreSettings

    @Binds
    val AppSettings.bindMediaDataSettings: MediaDataSettings

    @Binds
    val AppSettings.bindMonetizationSettings: MonetizationSettings

    @Binds
    val AppSettings.bindNewsSettings: NewsSettings

    @Binds
    val AppSettings.bindNetworkSettings: NetworkSettings

    @Binds
    val AppSettings.bindStaffSettings: StaffSettings
}
