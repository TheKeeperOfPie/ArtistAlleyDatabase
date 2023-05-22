package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StaffDetailsViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val animeSettings: AnimeSettings,
) : ViewModel() {

    lateinit var staffId: String

    var entry by mutableStateOf<StaffDetailsScreen.Entry?>(null)
    var loading by mutableStateOf(true)
    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()
    val showAdult get() = animeSettings.showAdult

    fun initialize(staffId: String) {
        if (::staffId.isInitialized) return
        this.staffId = staffId

        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                val staff = aniListApi.staffDetails(staffId)
                showAdult.collectLatest {
                    val entry = StaffDetailsScreen.Entry(staff, it)
                    withContext(CustomDispatchers.Main) {
                        this@StaffDetailsViewModel.entry = entry
                    }
                }
            } catch (exception: Exception) {
                withContext(CustomDispatchers.Main) {
                    errorResource = R.string.anime_staff_error_loading to exception
                }
            } finally {
                withContext(CustomDispatchers.Main) {
                    loading = false
                }
            }
        }
    }
}
