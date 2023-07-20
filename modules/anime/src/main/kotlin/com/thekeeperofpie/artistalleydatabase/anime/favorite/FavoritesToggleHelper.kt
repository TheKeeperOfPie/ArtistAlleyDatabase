package com.thekeeperofpie.artistalleydatabase.anime.favorite

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesToggleHelper(
    private val aniListApi: AuthedAniListApi,
    private val favoritesController: FavoritesController,
    private val scope: CoroutineScope,
) {
    companion object {
        private const val TAG = "FavoritesToggleHelper"
    }

    var favorite by mutableStateOf<Boolean?>(null)
        private set

    private var initializedTracking = false

    private val jobs = mutableMapOf<Pair<FavoriteType, String>, Job>()

    fun set(type: FavoriteType, id: String, favorite: Boolean) {
        val update = FavoritesController.Update(type, id, favorite, pending = true)
        favoritesController.onUpdate(update)
        val job = jobs[type to id]
        jobs[type to id] = scope.launch(CustomDispatchers.IO) {
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
        }
    }

    fun <Entry> initializeTracking(
        viewModel: ViewModel,
        entry: () -> Flow<Entry?>,
        entryToId: (Entry) -> String,
        entryToType: (Entry) -> FavoriteType,
        entryToFavorite: (Entry) -> Boolean,
    ) {
        if (initializedTracking) return
        initializedTracking = true
        viewModel.viewModelScope.launch(CustomDispatchers.Main) {
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
