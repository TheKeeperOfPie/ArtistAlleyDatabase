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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
        viewModelScope.launch(Dispatchers.IO) {
            val artists = artEntryDao.getArtists().toDistinctSortedResult()
//            sources = artEntryDao.getArtists().flatMap(JsonUtils::readStringList)
            val series = artEntryDao.getSeries().toDistinctSortedResult()
            val characters = artEntryDao.getCharacters().toDistinctSortedResult()
            val tags = artEntryDao.getTags().toDistinctSortedResult()

            withContext(Dispatchers.Main) {
                this@BrowseViewModel.artists = artists
                this@BrowseViewModel.series = series
                this@BrowseViewModel.characters = characters
                this@BrowseViewModel.tags = tags
            }
        }
    }

    private fun List<String>.toDistinctSortedResult() =
        flatMap(JsonUtils::readStringList).distinct().sorted()
}