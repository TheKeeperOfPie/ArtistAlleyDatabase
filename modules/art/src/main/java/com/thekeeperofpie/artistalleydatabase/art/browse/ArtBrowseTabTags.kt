package com.thekeeperofpie.artistalleydatabase.art.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.JsonUtils
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.art.R
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ArtBrowseTabTags @Inject constructor(
    artEntryDao: ArtEntryBrowseDao,
    artEntryNavigator: ArtEntryNavigator,
) : BrowseTabViewModel() {

    override val priorityMajor = 0
    override val priorityMinor = 3

    override val tab = BrowseScreen.TabContent(
        "art_entry_browse_tags",
        { R.string.art_browse_tab_tags },
        { tags },
        artEntryNavigator::navigate,
    )

    private var tags by mutableStateOf(emptyList<BrowseEntryModel>())

    init {
        viewModelScope.launch(Dispatchers.Main) {
            artEntryDao.getTags()
                .map {
                    it.flatMap(JsonUtils::readStringList).distinct()
                        .sortedWith(String.CASE_INSENSITIVE_ORDER)
                        .map {
                            BrowseEntryModel(
                                image = null,
                                text = it,
                                queryType = ArtEntryColumn.TAGS.toString(),
                            )
                        }
                }
                .collectLatest { tags = it }
        }
    }
}