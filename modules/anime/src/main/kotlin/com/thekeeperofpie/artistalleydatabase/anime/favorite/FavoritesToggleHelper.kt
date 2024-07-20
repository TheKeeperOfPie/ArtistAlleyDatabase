package com.thekeeperofpie.artistalleydatabase.anime.favorite

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.util.Collections

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesToggleHelper(
    private val aniListApi: AuthedAniListApi,
    private val favoritesController: FavoritesController,
    private val scope: CoroutineScope,
) {
    companion object {
        private const val TAG = "FavoritesToggleHelper"
    }

    // TODO: Initial with media value?
    var favorite by mutableStateOf<Boolean?>(null)
        private set

    private var initializedTracking = false

    private val jobs = Collections.synchronizedMap(mutableMapOf<Pair<FavoriteType, String>, Job>())

    fun set(type: FavoriteType, id: String, favorite: Boolean) {
        val update = FavoritesController.Update(type, id, favorite, pending = true)
        favoritesController.onUpdate(update)
        val jobKey = type to id
        val job = jobs[jobKey]
        jobs[jobKey] = scope.launch(CustomDispatchers.IO) {
            job?.cancelAndJoin()
            try {
                val result = when (type) {
                    FavoriteType.ANIME -> aniListApi.toggleAnimeFavorite(id)
                    FavoriteType.MANGA -> aniListApi.toggleMangaFavorite(id)
                    FavoriteType.CHARACTER -> aniListApi.toggleCharacterFavorite(id)
                    FavoriteType.STAFF -> aniListApi.toggleStaffFavorite(id)
                    FavoriteType.STUDIO -> aniListApi.toggleStudioFavorite(id)
                }
                favoritesController.onUpdate(update.copy(favorite = result, pending = false))
            } catch (e: Throwable) {
                Log.e(TAG, "Error toggling favorite", e)
                favoritesController.onUpdate(
                    update.copy(favorite = !favorite, pending = false, error = e)
                )
            }
            jobs.remove(jobKey)
        }
    }

    fun <Entry> initializeTracking(
        scope: CoroutineScope,
        entry: () -> Flow<Entry?>,
        entryToId: (Entry) -> String,
        entryToType: (Entry) -> FavoriteType,
        entryToFavorite: (Entry) -> Boolean,
    ) {
        if (initializedTracking) return
        initializedTracking = true
        scope.launch(CustomDispatchers.Main) {
            entry()
                .filterNotNull()
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { entry ->
                    favoritesController.changes(entryToType(entry), entryToId(entry))
                        .mapLatest { it?.favorite ?: entryToFavorite(entry) }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { favorite = it }
        }
    }
}
