package com.thekeeperofpie.artistalleydatabase.anime.character.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AnimeCharacterDetailsViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
    favoritesController: FavoritesController,
    private val ignoreList: AnimeMediaIgnoreList,
    private val settings: AnimeSettings,
) : ViewModel() {

    val viewer = aniListApi.authedUser

    lateinit var characterId: String

    var entry by mutableStateOf<CharacterDetailsScreen.Entry?>(null)
    var loading by mutableStateOf(true)
    var errorResource by mutableStateOf<Pair<Int, Throwable?>?>(null)
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    fun initialize(characterId: String, favorite: Boolean?) {
        if (::characterId.isInitialized) return
        this.characterId = characterId

        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                val character = aniListApi.characterDetails(characterId)
                val media = character.media?.edges
                    ?.distinctBy { it?.node?.id }
                    ?.mapNotNull { it?.node?.let(AnimeMediaListRow::Entry) }
                    .orEmpty()

                combine(
                    statusController.allChanges(media.map { it.media.id.toString() }.toSet()),
                    ignoreList.updates,
                    settings.showAdult,
                ) { statuses, ignoredIds, showAdult ->
                    CharacterDetailsScreen.Entry(
                        character,
                        media = media.mapNotNull {
                            applyMediaFiltering(
                                statuses = statuses,
                                ignoredIds = ignoredIds,
                                showAdult = showAdult,
                                showIgnored = true,
                                entry = it,
                                transform = { it },
                                media = it.media,
                                copy = { mediaListStatus, progress, progressVolumes, ignored ->
                                    AnimeMediaListRow.Entry(
                                        media = this.media,
                                        mediaListStatus = mediaListStatus,
                                        progress = progress,
                                        progressVolumes = progressVolumes,
                                        ignored = ignored,
                                    )
                                }
                            )
                        },
                    )
                }
                    .collectLatest {
                        withContext(CustomDispatchers.Main) {
                            this@AnimeCharacterDetailsViewModel.entry = it
                        }
                    }
            } catch (exception: Exception) {
                withContext(CustomDispatchers.Main) {
                    errorResource = R.string.anime_character_error_loading to exception
                }
            } finally {
                withContext(CustomDispatchers.Main) {
                    loading = false
                }
            }
        }

        favoritesToggleHelper.initializeTracking(
            viewModel = this,
            entry = { snapshotFlow { entry } },
            entryToId = { it.character.id.toString() },
            entryToType = { FavoriteType.CHARACTER },
            entryToFavorite = { it.character.isFavourite },
        )
    }

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry<*>) =
        ignoreList.toggle(entry.media.id.toString())
}
