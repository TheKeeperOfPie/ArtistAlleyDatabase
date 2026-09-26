package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.user.MerchUserEntry
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@AssistedInject
class ArtistMerchViewModel(
    dispatchers: CustomDispatchers,
    merchEntryDao: MerchEntryDao,
    userEntryDao: UserEntryDao,
    @Assisted val merch: String,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val merchEntry = merchEntryDao.getMerchById(merch)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val mutationUpdates = MutableSharedFlow<MerchUserEntry>(5, 5)

    init {
        viewModelScope.launch(dispatchers.io) {
            mutationUpdates.collectLatest {
                userEntryDao.insertMerchUserEntry(it)
            }
        }
    }

    fun onFavoriteToggle(data: MerchWithUserData, favorite: Boolean) {
        mutationUpdates.tryEmit(data.userEntry.copy(favorite = favorite))
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(merch: String, savedStateHandle: SavedStateHandle): ArtistMerchViewModel
    }
}
