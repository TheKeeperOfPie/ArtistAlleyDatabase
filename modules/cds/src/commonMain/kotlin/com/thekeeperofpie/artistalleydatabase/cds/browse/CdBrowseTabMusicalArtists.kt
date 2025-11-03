package com.thekeeperofpie.artistalleydatabase.cds.browse

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.map
import artistalleydatabase.modules.cds.generated.resources.Res
import artistalleydatabase.modules.cds.generated.resources.cd_browse_tab_musical_artists
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryNavigator
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryColumn
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtist
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDao
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbUtils
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtistDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@SingleIn(AppScope::class)
@Inject
class CdBrowseTabMusicalArtists(
    musicalArtistDao: MusicalArtistDao,
    vgmdbArtistDao: VgmdbArtistDao,
    cdEntryNavigator: CdEntryNavigator,
) : BrowseTabViewModel() {

    override val priorityMajor = 1
    override val priorityMinor = 0

    override val tab = BrowseScreen.TabContent(
        "cd_entry_browse_musical_artists",
        { Res.string.cd_browse_tab_musical_artists },
        { Either.Right(musicalArtists.collectAsLazyPagingItems()) },
        cdEntryNavigator::navigate,
    )

    private var musicalArtists = MutableStateFlow(PagingData.empty<BrowseEntryModel>())

    init {
        viewModelScope.launch(Dispatchers.Main) {
            @OptIn(ExperimentalCoroutinesApi::class)
            initializationBarrier()
                .flatMapLatest {
                    Pager(PagingConfig(pageSize = 20)) { musicalArtistDao.getEntries() }.flow
                }
                .cachedIn(viewModelScope)
                .map {
                    it.map(Dispatchers.IO.asExecutor()) {
                        when (it.type) {
                            MusicalArtist.Type.CUSTOM -> BrowseEntryModel(
                                image = it.image,
                                text = it.name,
                                queryType = CdEntryColumn.PERFORMERS.toString(),
                                queryIdOrString = Either.Right(it.id.removePrefix("custom_")),
                            )
                            MusicalArtist.Type.VGMDB -> {
                                val realId = it.id.removePrefix("vgmdb_")
                                val entry = runBlocking { vgmdbArtistDao.getEntry(realId) }
                                if (entry != null) {
                                    BrowseEntryModel(
                                        image = entry.pictureThumb,
                                        link = VgmdbUtils.artistUrl(entry.id),
                                        text = entry.name,
                                        queryType = CdEntryColumn.PERFORMERS.toString(),
                                        queryIdOrString = Either.Left(entry.id),
                                    )
                                } else {
                                    BrowseEntryModel(
                                        image = it.image,
                                        text = it.name,
                                        queryType = CdEntryColumn.PERFORMERS.toString(),
                                        queryIdOrString = Either.Left(realId)
                                    )
                                }
                            }
                        }
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect(musicalArtists)
        }
    }
}
