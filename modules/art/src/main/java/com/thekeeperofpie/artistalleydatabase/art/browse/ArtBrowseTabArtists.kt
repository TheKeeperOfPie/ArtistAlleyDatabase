package com.thekeeperofpie.artistalleydatabase.art.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.JsonUtils
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.art.R
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.toUri
import com.thekeeperofpie.artistalleydatabase.utils_compose.StringResourceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class ArtBrowseTabArtists(
    appFileSystem: AppFileSystem,
    artEntryDao: ArtEntryBrowseDao,
    artEntryNavigator: ArtEntryNavigator,
) : BrowseTabViewModel() {

    override val priorityMajor = 0
    override val priorityMinor = 0

    override val tab = BrowseScreen.TabContent(
        "art_entry_browse_artists",
        { StringResourceId(R.string.art_browse_tab_artists) },
        { Either.Left(artists) },
        artEntryNavigator::navigate,
    )

    private var artists by mutableStateOf(emptyList<BrowseEntryModel>())

    init {
        viewModelScope.launch(Dispatchers.Main) {
            @OptIn(ExperimentalCoroutinesApi::class)
            initializationBarrier()
                .flatMapLatest { artEntryDao.getArtists() }
                .flatMapLatest {
                    it.flatMap(JsonUtils::readStringList)
                        .map { entry ->
                            artEntryDao.getArtistFlow(entry)
                                .flatMapLatest { it.asFlow() }
                                .take(1)
                                .map {
                                    BrowseEntryModel(
                                        image = EntryUtils.getImagePath(appFileSystem, it.entryId)
                                            ?.toUri()?.toString(),
                                        text = entry,
                                        queryType = ArtEntryColumn.ARTISTS.toString(),
                                    )
                                }
                                .startWith(
                                    BrowseEntryModel(
                                        image = null,
                                        text = entry,
                                        queryType = ArtEntryColumn.ARTISTS.toString(),
                                    )
                                )
                        }
                        .let {
                            combine(it) {
                                it.distinctBy { it.queryIdOrString }.sortedByText()
                            }
                        }
                }
                .flowOn(Dispatchers.IO)
                .collectLatest { artists = it }
        }
    }
}
