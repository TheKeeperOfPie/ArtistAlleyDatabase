package com.thekeeperofpie.artistalleydatabase.anime.favorite

import com.hoc081098.flowext.startWith
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter

class FavoritesController {

    private val updates = MutableSharedFlow<Update>(replay = 0, extraBufferCapacity = 5)

    fun onUpdate(update: Update) = updates.tryEmit(update)

    // TODO: Accept a default state so that it can emit immediately without a null value?
    fun changes(type: FavoriteType, id: String) = updates.filter { it.type == type && it.id == id }
        .startWith(null)

    data class Update(
        val type: FavoriteType,
        val id: String,
        val favorite: Boolean,
        val pending: Boolean,
        val error: Throwable? = null,
    )
}
