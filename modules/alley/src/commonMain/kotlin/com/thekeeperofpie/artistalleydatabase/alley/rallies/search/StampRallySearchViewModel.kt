package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.lifecycle.viewModelScope
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import app.cash.paging.filter
import app.cash.paging.map
import com.hoc081098.flowext.defer
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchViewModel
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class StampRallySearchViewModel(
    private val stampRallyEntryDao: StampRallyEntryDao,
    private val userEntryDao: UserEntryDao,
    private val settings: ArtistAlleySettings,
    @Assisted private val filterParams: StateFlow<StampRallySortFilterViewModel.FilterParams>,
) : EntrySearchViewModel<StampRallySearchQuery, StampRallyEntryGridModel>() {

    override val sections = emptyList<EntrySection>()

    val displayType = settings.displayType
    val randomSeed = Random.nextInt().absoluteValue
    private val mutationUpdates = MutableSharedFlow<StampRallyUserEntry>(5, 5)

    val dataYear = settings.dataYear

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            mutationUpdates.collectLatest {
                userEntryDao.insertStampRallyUserEntry(it)
            }
        }
    }

    override fun searchOptions() = defer {
        filterParams.mapLatest {
            StampRallySearchQuery(
                filterParams = it,
                randomSeed = randomSeed,
            )
        }
    }

    override fun mapQuery(
        query: String,
        options: StampRallySearchQuery,
    ) = settings.dataYear
        .flatMapLatest {
            createPager(createPagingConfig(pageSize = PlatformSpecificConfig.defaultPageSize)) {
                stampRallyEntryDao.search(year = it, query = query, searchQuery = options)
            }.flow
        }
        .flowOn(CustomDispatchers.IO)
        .map { it.filter { !it.userEntry.ignored || options.filterParams.showIgnored } }
        .map { it.map { StampRallyEntryGridModel.buildFromEntry(it) } }
        .cachedIn(viewModelScope)

    fun onFavoriteToggle(entry: StampRallyEntryGridModel, favorite: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(favorite = favorite))
    }

    fun onIgnoredToggle(entry: StampRallyEntryGridModel, ignored: Boolean) {
        mutationUpdates.tryEmit(entry.userEntry.copy(ignored = ignored))
    }

    fun onDisplayTypeToggle(displayType: SearchScreen.DisplayType) {
        settings.displayType.value = displayType.name
    }
}
