package com.thekeeperofpie.artistalleydatabase

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionCharacters
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionStaff
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionVoiceActor
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGenrePreview
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaTagPreview
import com.thekeeperofpie.artistalleydatabase.markdown.LocalMarkdown
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.LocalImageColorsState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberImageColorsState

@Composable
fun SharedInfra(component: AppComponent, content: @Composable () -> Unit) {
    val settings = component.settings
    val languageOptionMedia by settings.languageOptionMedia.collectAsState()
    val languageOptionCharacters by settings.languageOptionCharacters.collectAsState()
    val languageOptionStaff by settings.languageOptionStaff.collectAsState()
    val languageOptionVoiceActor by settings.languageOptionVoiceActor.collectAsState()
    val showFallbackVoiceActor by settings.showFallbackVoiceActor.collectAsState()

    val mediaGenreDialogController = component.mediaGenreDialogController
    val mediaTagDialogController = component.mediaTagDialogController
    val ignoreController = component.ignoreController
    val markdown = component.markdown
    val fullscreenImageHandler = component.fullscreenImageHandler

    val imageColorsState = rememberImageColorsState()

    CompositionLocalProvider(
        LocalLanguageOptionMedia provides languageOptionMedia,
        LocalLanguageOptionCharacters provides languageOptionCharacters,
        LocalLanguageOptionStaff provides languageOptionStaff,
        LocalLanguageOptionVoiceActor provides
                (languageOptionVoiceActor to showFallbackVoiceActor),
        LocalMediaTagDialogController provides mediaTagDialogController,
        LocalMediaGenreDialogController provides mediaGenreDialogController,
        LocalMarkdown provides markdown,
        LocalIgnoreController provides ignoreController,
        LocalImageColorsState provides imageColorsState,
        LocalFullscreenImageHandler provides fullscreenImageHandler,
    ) {
        content()

        val tagShown = mediaTagDialogController.tagShown
        if (tagShown != null) {
            MediaTagPreview(
                tag = tagShown,
                onDismiss = { mediaTagDialogController.tagShown = null },
            )
        }

        val genreShown = mediaGenreDialogController.genreShown
        if (genreShown != null) {
            MediaGenrePreview(
                genre = genreShown,
                onDismiss = { mediaGenreDialogController.genreShown = null },
            )
        }

        fullscreenImageHandler.ImageDialog()
    }
}
