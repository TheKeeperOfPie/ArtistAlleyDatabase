package com.thekeeperofpie.artistalleydatabase.anime.songs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// State separated from immutable data so that recomposition is as granular as possible
class AnimeSongState(
    val id: String,
    val entry: AnimeSongEntry,
) {
    private var _expanded by mutableStateOf(false)

    fun expanded() = _expanded

    fun setExpanded(expanded: Boolean) {
        this._expanded = expanded
    }
}
