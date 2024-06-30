package com.thekeeperofpie.artistalleydatabase.alley.map.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavoritesMapViewModel @Inject constructor(settings: ArtistAlleySettings) : ViewModel() {

    val sortFilterController = FavoritesSortFilterController(viewModelScope, settings)
}
