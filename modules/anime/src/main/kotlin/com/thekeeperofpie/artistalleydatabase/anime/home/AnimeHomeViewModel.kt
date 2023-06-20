package com.thekeeperofpie.artistalleydatabase.anime.home

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AnimeHomeViewModel @Inject constructor() : ViewModel() {

    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()
}
