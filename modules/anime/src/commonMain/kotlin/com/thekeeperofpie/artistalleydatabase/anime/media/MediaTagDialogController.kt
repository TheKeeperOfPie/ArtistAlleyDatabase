package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaTagSection
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class MediaTagDialogController(
    private val mediaTagsController: MediaTagsController,
){
    var tagShown by mutableStateOf<MediaTagSection.Tag?>(null)

    fun onLongClickTag(tagId: String) {
        tagShown = mediaTagsController.tags.replayCache.firstOrNull().orEmpty().values
            .asSequence()
            .mapNotNull { it.findTag(tagId) }
            .firstOrNull()
    }
}

val LocalMediaTagDialogController = staticCompositionLocalOf<MediaTagDialogController?> { null }
