package com.thekeeperofpie.artistalleydatabase.add

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEntryViewModel @Inject constructor(
    application: Application,
    artEntryDao: ArtEntryDao
) : ArtEntryViewModel(application, artEntryDao) {

    val imageUris = mutableStateListOf<Uri>()

    fun onClickSave(navHostController: NavHostController) {
        viewModelScope.launch(Dispatchers.IO) {
            imageUris.forEach {
                val id = UUID.randomUUID().toString()
                saveEntry(it, id)
            }

            withContext(Dispatchers.Main) {
                navHostController.popBackStack()
            }
        }
    }
}