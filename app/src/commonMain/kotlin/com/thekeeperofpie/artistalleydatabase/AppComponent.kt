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
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.utils_compose.FullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import me.tatarka.inject.annotations.Provides

interface AppComponent {
    val settings: AnimeSettings
    val mediaGenreDialogController: MediaGenreDialogController
    val mediaTagDialogController: MediaTagDialogController
    val ignoreController: IgnoreController
    val markdown: Markdown
    val fullscreenImageHandler: FullscreenImageHandler

    val AppSettings.bindAniListSettings: AniListSettings
        @Provides get() = this

    val AppSettings.bindAnimeSettings: AnimeSettings
        @Provides get() = this

    val AppSettings.bindCharacterSettings: CharacterSettings
        @Provides get() = this

    val AppSettings.bindCropSettings: CropSettings
        @Provides get() = this

    val AppSettings.bindHistorySettings: HistorySettings
        @Provides get() = this

    val AppSettings.bindIgnoreSettings: IgnoreSettings
        @Provides get() = this

    val AppSettings.bindMediaDataSettings: MediaDataSettings
        @Provides get() = this

    val AppSettings.bindMonetizationSettings: MonetizationSettings
        @Provides get() = this

    val AppSettings.bindNewsSettings: NewsSettings
        @Provides get() = this

    val AppSettings.bindNetworkSettings: NetworkSettings
        @Provides get() = this

    val AppSettings.bindStaffSettings: StaffSettings
        @Provides get() = this
}
