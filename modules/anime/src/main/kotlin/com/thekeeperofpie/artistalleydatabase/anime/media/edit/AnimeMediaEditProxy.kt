package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import com.anilist.fragment.MediaDetailsListEntry
import kotlinx.coroutines.flow.MutableStateFlow

// TODO: Find a better way to send update back to details screen
object AnimeMediaEditProxy {

    val editResults = MutableStateFlow<Pair<String, MediaDetailsListEntry?>?>(null)
}
