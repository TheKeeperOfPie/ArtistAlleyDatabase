package com.thekeeperofpie.artistalleydatabase.alley.edit.merch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@AssistedInject
class MerchResolutionViewModel(
    private val artistCache: ArtistCache,
    private val editDatabase: AlleyEditDatabase,
    dispatchers: CustomDispatchers,
    private val tagAutocomplete: TagAutocomplete,
    @Assisted private val merchId: String,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val commitJob = ExclusiveProgressJob(viewModelScope, ::commit)

    val artists = flowFromSuspend { loadArtists() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val progress get() = commitJob.state

    fun merchPredictions(query: String) =
        tagAutocomplete.merchPredictions(query, allowCustom = false)

    fun onClickDone(merch: MerchInfo) = commitJob.launch { merch }

    private fun loadArtists() = artistCache.artistsAnimeExpo2026.value.filter {
        it.merchInferred.contains(merchId) ||
                it.merchConfirmed.contains(merchId)
    }

    private suspend fun commit(merch: MerchInfo): BackendRequest.ArtistSave.Response {
        loadArtists().forEach {
            val artist = editDatabase.loadArtist(DataYear.ANIME_EXPO_2026, it.id) ?: return@forEach
            val response = editDatabase.saveArtist(
                dataYear = DataYear.ANIME_EXPO_2026,
                initial = artist,
                updated = artist.copy(
                    merchInferred = artist.merchInferred.map { if (it == merchId) merch.name else it },
                    merchConfirmed = artist.merchConfirmed.map { if (it == merchId) merch.name else it },
                )
            )
            if (response != BackendRequest.ArtistSave.Response.Success) {
                return@commit response
            }
        }

        return BackendRequest.ArtistSave.Response.Success
    }

    @AssistedFactory
    interface Factory {
        fun create(merchId: String, savedStateHandle: SavedStateHandle): MerchResolutionViewModel
    }
}
