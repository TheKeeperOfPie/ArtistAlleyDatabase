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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AnimeCharacterDetailsViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
) : ViewModel() {

    lateinit var characterId: String

    var entry by mutableStateOf<CharacterDetailsScreen.Entry?>(null)
    var loading by mutableStateOf(true)
    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    fun initialize(characterId: String) {
        if (::characterId.isInitialized) return
        this.characterId = characterId

        viewModelScope.launch(CustomDispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                val character = aniListApi.characterDetails(characterId)
                val entry = CharacterDetailsScreen.Entry(character)
                val endTime = System.currentTimeMillis()
                val timeDifference = endTime - startTime
                if (timeDifference < 450) {
                    // Prevent shared transition from previous screen for lazily loaded content
                    // TODO: Find a better way to prevent shared transitions for lazy content
                    delay((450 - timeDifference).milliseconds)
                }
                withContext(CustomDispatchers.Main) {
                    this@AnimeCharacterDetailsViewModel.entry = entry
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
}
