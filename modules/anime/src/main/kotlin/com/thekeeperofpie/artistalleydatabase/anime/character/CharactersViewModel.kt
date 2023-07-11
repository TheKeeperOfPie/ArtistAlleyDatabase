package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
) : ViewModel() {

    lateinit var mediaId: String

    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    var entry by mutableStateOf<CharactersScreen.Entry?>(null)
        private set

    val characters = MutableStateFlow(PagingData.empty<DetailsCharacter>())

    var error by mutableStateOf<Pair<Int, Exception?>?>(null)

    // TODO: Refresh support
    // TODO: Pass the media information through memory rather than re-fetching
    fun initialize(mediaId: String) {
        if (::mediaId.isInitialized) return
        this.mediaId = mediaId

        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                val entry = CharactersScreen.Entry(
                    aniListApi.mediaAndCharacters(mediaId).media!!
                )
                withContext(CustomDispatchers.Main) {
                    this@CharactersViewModel.entry = entry
                }

                Pager(config = PagingConfig(10)) {
                    AniListPagingSource {
                        if (it == 1) {
                            val result = entry.media.characters
                            result?.pageInfo to result?.edges?.filterNotNull()
                                ?.let {
                                    CharacterUtils.toDetailsCharacters(it) { it.role }
                                }
                                .orEmpty()
                        } else {
                            val result = aniListApi.mediaAndCharactersPage(
                                mediaId = mediaId,
                                page = it
                            ).media?.characters
                            result?.pageInfo to result?.edges?.filterNotNull()
                                ?.let {
                                    CharacterUtils.toDetailsCharacters(it) { it.role }
                                }
                                .orEmpty()
                        }
                    }
                }.flow
                    .map {
                        // AniList can return duplicates across pages, manually enforce uniqueness
                        val seenIds = mutableSetOf<String>()
                        it.filter { seenIds.add(it.id) }
                    }
                    .cachedIn(viewModelScope)
                    .collectLatest(characters::emit)
            } catch (e: Exception) {
                withContext(CustomDispatchers.Main) {
                    error = R.string.anime_characters_error_loading to e
                }
            }
        }
    }
}
