package com.thekeeperofpie.artistalleydatabase.anime.character.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaLargeCard.Entry.Loading.ignored
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnimeCharacterDetailsViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
    private val ignoreList: AnimeMediaIgnoreList,
) : ViewModel() {

    lateinit var characterId: String

    var entry by mutableStateOf<CharacterDetailsScreen.Entry?>(null)
    var loading by mutableStateOf(true)
    var errorResource by mutableStateOf<Pair<Int, Throwable?>?>(null)
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    fun initialize(characterId: String) {
        if (::characterId.isInitialized) return
        this.characterId = characterId

        viewModelScope.launch(CustomDispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                val character = aniListApi.characterDetails(characterId)
                val media = character.media?.edges
                    ?.distinctBy { it?.node?.id }
                    ?.mapNotNull { it?.node?.let(AnimeMediaListRow::Entry) }
                    .orEmpty()

                val endTime = System.currentTimeMillis()
                val timeDifference = endTime - startTime
                if (timeDifference < 450) {
                    // Prevent shared transition from previous screen for lazily loaded content
                    // TODO: Find a better way to prevent shared transitions for lazy content
                    delay((450 - timeDifference).milliseconds)
                }
                statusController.allChanges(media.map { it.media.id.toString() }.toSet())
                    .mapLatest { statuses ->
                        CharacterDetailsScreen.Entry(character, media = media.map {
                            val mediaId = it.media.id.toString()
                            if (statuses.containsKey(mediaId)) {
                                AnimeMediaListRow.Entry(
                                    media = it.media,
                                    mediaListStatus = statuses[mediaId],
                                    ignored = ignored
                                )
                            } else it
                        })
                    }
                    .collectLatest {
                        withContext(CustomDispatchers.Main) {
                            this@AnimeCharacterDetailsViewModel.entry = entry
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
    }

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry<*>) =
        ignoreList.toggle(entry.media.id.toString())
}
