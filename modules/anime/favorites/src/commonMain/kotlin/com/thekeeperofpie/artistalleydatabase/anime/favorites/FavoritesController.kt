package com.thekeeperofpie.artistalleydatabase.anime.favorites

import com.hoc081098.flowext.startWith
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter

@SingleIn(AppScope::class)
@Inject
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
