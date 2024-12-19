package com.thekeeperofpie.artistalleydatabase

import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.utils_compose.FullscreenImageHandler

interface AppComponent {
    val settings: AnimeSettings
    val mediaGenreDialogController: MediaGenreDialogController
    val mediaTagDialogController: MediaTagDialogController
    val ignoreController: IgnoreController
    val markdown: Markdown
    val fullscreenImageHandler: FullscreenImageHandler
}
