package com.thekeeperofpie.artistalleydatabase.art.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.android_utils.JsonUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.art.R
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import javax.inject.Inject

class ArtBrowseTabSeries @Inject constructor(
    artEntryDao: ArtEntryBrowseDao,
    artEntryNavigator: ArtEntryNavigator,
    appJson: AppJson,
    mediaRepository: MediaRepository,
) : BrowseTabViewModel() {

    override val priorityMajor = 0
    override val priorityMinor = 1

    override val tab = BrowseScreen.TabContent(
        "art_entry_browse_series",
        { R.string.art_browse_tab_series },
        { series },
        artEntryNavigator::navigate,
    )

    private var series by mutableStateOf(emptyList<BrowseEntryModel>())

    init {
        viewModelScope.launch(Dispatchers.Main) {
            @Suppress("OPT_IN_USAGE")
            artEntryDao.getSeries()
                .flatMapLatest {
                    it.flatMap(JsonUtils::readStringList)
                        .distinct()
                        .map { databaseText ->
                            val entry = databaseText.takeIf { it.contains("{") }
                                ?.let<String, MediaColumnEntry>(appJson.json::decodeFromString)
                            entry?.let {
                                mediaRepository.getEntry(it.id)
                                    .filterNotNull()
                                    .map {
                                        BrowseEntryModel(
                                            image = it.image?.medium,
                                            link = AniListUtils.mediaUrl(it.type, it.id),
                                            text = it.title?.romaji ?: databaseText,
                                            queryType = ArtEntryColumn.SERIES.toString(),
                                            queryIdOrString = Either.Left(it.id),
                                        )
                                    }
                            }
                                .let { it ?: emptyFlow() }
                                .onStart {
                                    if (entry == null) {
                                        BrowseEntryModel(
                                            text = databaseText,
                                            queryType = ArtEntryColumn.SERIES.toString(),
                                        )
                                    } else {
                                        BrowseEntryModel(
                                            text = entry.title,
                                            queryType = ArtEntryColumn.SERIES.toString(),
                                            queryIdOrString = Either.Left(entry.id),
                                        )
                                    }.let { emit(it) }
                                }
                        }
                        .let {
                            combine(it) {
                                it.distinctBy { it.queryIdOrString }.sortedByText()
                            }
                        }
                }
                .collectLatest { series = it }
        }
    }
}