package com.thekeeperofpie.artistalleydatabase.art.browse

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.android_utils.JsonUtils
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.art.R
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
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
import javax.inject.Inject

class ArtBrowseTabTags @Inject constructor(
    context: Application,
    artEntryDao: ArtEntryBrowseDao,
    artEntryNavigator: ArtEntryNavigator,
) : BrowseTabViewModel() {

    override val priorityMajor = 0
    override val priorityMinor = 3

    override val tab = BrowseScreen.TabContent(
        "art_entry_browse_tags",
        { R.string.art_browse_tab_tags },
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
                    it.flatMap(JsonUtils::readStringList)
                        .map { entry ->
                            artEntryDao.getTagFlow(entry)
                                .flatMapLatest { it.asFlow() }
                                .take(1)
                                .map {
                                    BrowseEntryModel(
                                        image = ArtEntryUtils.getImageFile(context, it.entryId)
                                            .toUri().toString(),
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