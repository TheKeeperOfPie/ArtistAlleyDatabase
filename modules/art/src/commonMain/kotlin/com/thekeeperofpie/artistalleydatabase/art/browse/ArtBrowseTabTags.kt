package com.thekeeperofpie.artistalleydatabase.art.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.art.generated.resources.Res
import artistalleydatabase.modules.art.generated.resources.art_browse_tab_tags
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.toUri
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.parseStringList
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Inject
class ArtBrowseTabTags(
    appFileSystem: AppFileSystem,
    artEntryDao: ArtEntryBrowseDao,
    artEntryNavigator: ArtEntryNavigator,
    json: Json,
) : BrowseTabViewModel() {

    override val priorityMajor = 0
    override val priorityMinor = 3

    override val tab = BrowseScreen.TabContent(
        "art_entry_browse_tags",
        { Res.string.art_browse_tab_tags },
        { Either.Left(tags) },
        artEntryNavigator::navigate,
    )

    private var tags by mutableStateOf(emptyList<BrowseEntryModel>())

    init {
        viewModelScope.launch(Dispatchers.Main) {
            @OptIn(ExperimentalCoroutinesApi::class)
            initializationBarrier()
                .flatMapLatest { artEntryDao.getTags() }
                .flatMapLatest {
                    it.flatMap(json::parseStringList)
                        .map { entry ->
                            artEntryDao.getTagFlow(entry)
                                .flatMapLatest { it.asFlow() }
                                .take(1)
                                .map {
                                    BrowseEntryModel(
                                        image = EntryUtils.getImagePath(appFileSystem, it.entryId)
                                            ?.toUri()?.toString(),
                                        text = entry,
                                        queryType = ArtEntryColumn.TAGS.toString(),
                                    )
                                }
                                .startWith(
                                    BrowseEntryModel(
                                        image = null,
                                        text = entry,
                                        queryType = ArtEntryColumn.TAGS.toString(),
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
                .collectLatest { tags = it }
        }
    }
}
