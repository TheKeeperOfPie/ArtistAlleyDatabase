package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class MediaTagDialogController(
    private val mediaTagsController: MediaTagsController,
) {
    var tagShown by mutableStateOf<MediaTagEntry.Tag?>(null)

    fun onLongClickTag(tagId: String) {
        tagShown = mediaTagsController.tags.replayCache.firstOrNull()
            .orEmpty()
            .map { it.second }
            .asSequence()
            .mapNotNull { it.findTag(tagId) }
            .map { it as MediaTagEntry.Tag }
            .firstOrNull()
    }
}

val LocalMediaTagDialogController = staticCompositionLocalOf<MediaTagDialogController?> { null }
