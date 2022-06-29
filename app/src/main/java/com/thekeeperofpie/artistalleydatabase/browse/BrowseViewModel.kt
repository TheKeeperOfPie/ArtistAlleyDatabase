package com.thekeeperofpie.artistalleydatabase.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.JsonUtils
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KMutableProperty0

@HiltViewModel
class BrowseViewModel @Inject constructor(artEntryDao: ArtEntryDao) : ViewModel() {

    var artists by mutableStateOf(emptyList<String>())
    var series by mutableStateOf(emptyList<String>())
    var characters by mutableStateOf(emptyList<String>())
    var tags by mutableStateOf(emptyList<String>())

    val tabs = listOf(
        BrowseScreen.TabContent(
            ArtEntryColumn.ARTISTS,
            { R.string.browse_tab_artists },
            { artists }),
        BrowseScreen.TabContent(
            ArtEntryColumn.SERIES,
            { R.string.browse_tab_series },
            { series }
        ),
        BrowseScreen.TabContent(
            ArtEntryColumn.CHARACTERS,
            { R.string.browse_tab_characters },
            { characters }
        ),
        BrowseScreen.TabContent(
            ArtEntryColumn.TAGS,
            { R.string.browse_tab_tags },
            { tags }
        ),
    )

    init {
        subscribeColumn(artEntryDao::getArtists, this::artists)
        subscribeColumn(artEntryDao::getSeries, this::series)
        subscribeColumn(artEntryDao::getCharacters, this::characters)
        subscribeColumn(artEntryDao::getTags, this::tags)
    }

    private fun subscribeColumn(
        query: () -> Flow<List<String>>,
        property: KMutableProperty0<List<String>>
    ) = viewModelScope.launch(Dispatchers.Main) {
        query()
            .map { it.toDistinctSortedResult() }
            .flowOn(Dispatchers.IO)
            .collectLatest(property::set)
    }

    private fun List<String>.toDistinctSortedResult() =
        flatMap(JsonUtils::readStringList).distinct().sortedWith(String.CASE_INSENSITIVE_ORDER)
}