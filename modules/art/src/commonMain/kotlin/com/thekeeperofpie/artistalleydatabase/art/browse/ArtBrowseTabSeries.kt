package com.thekeeperofpie.artistalleydatabase.art.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.art.generated.resources.Res
import artistalleydatabase.modules.art.generated.resources.art_browse_tab_series
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.browse.BrowseEntryModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.data.Series
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Inject
class ArtBrowseTabSeries(
    appFileSystem: AppFileSystem,
    artEntryDao: ArtEntryBrowseDao,
    artEntryNavigator: ArtEntryNavigator,
    json: Json,
    mediaRepository: MediaRepository,
) : BrowseTabViewModel() {

    override val priorityMajor = 0
    override val priorityMinor = 1

    override val tab = BrowseScreen.TabContent(
        "art_entry_browse_series",
        { Res.string.art_browse_tab_series },
        { Either.Left(series) },
        artEntryNavigator::navigate,
    )

    private var series by mutableStateOf(emptyList<BrowseEntryModel>())

    init {
        viewModelScope.launch(Dispatchers.Main) {
            @OptIn(ExperimentalCoroutinesApi::class)
            initializationBarrier()
                .map { artEntryDao.getSeries() }
                .flatMapLatest {
                    it.flatMap(json::parseStringList)
                        .distinct()
                        .map { databaseText ->
                            val entry = databaseText.takeIf { it.contains("{") }
                                ?.let<String, MediaColumnEntry>(json::decodeFromString)
                            if (entry == null) {
                                artEntryDao.getSeriesFlow(databaseText, limit = 10)
                                    .flatMapLatest { it.asFlow() }
                                    .filter {
                                        it.series(json)
                                            .filterIsInstance<Series.Custom>()
                                            .any { it.text.contains(databaseText) }
                                    }
                                    .take(1)
                                    .map {
                                        BrowseEntryModel(
                                            image = EntryUtils.getImagePath(appFileSystem, it.entryId)
                                                ?.toUri()?.toString(),
                                            text = databaseText,
                                            queryType = ArtEntryColumn.SERIES.toString(),
                                        )
                                    }
                            } else {
                                mediaRepository.getEntry(entry.id)
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
                                .onStart {
                                    if (entry == null) {
                                        BrowseEntryModel(
                                            text = databaseText,
                                            queryType = ArtEntryColumn.SERIES.toString(),
                                        )
                                    } else {
                                        BrowseEntryModel(
                                            // TODO: Missing entry type
                                            // link = AniListUtils.mediaUrl(entry.type, entry.id),
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
                .flowOn(Dispatchers.IO)
                .collectLatest { series = it }
        }
    }
}
