package com.thekeeperofpie.artistalleydatabase.art

import androidx.lifecycle.SavedStateHandle
import com.thekeeperofpie.artistalleydatabase.art.browse.ArtBrowseTabArtists
import com.thekeeperofpie.artistalleydatabase.art.browse.ArtBrowseTabCharacters
import com.thekeeperofpie.artistalleydatabase.art.browse.ArtBrowseTabSeries
import com.thekeeperofpie.artistalleydatabase.art.browse.ArtBrowseTabTags
import com.thekeeperofpie.artistalleydatabase.art.browse.selection.ArtBrowseSelectionViewModel
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDatabase
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntrySyncDao
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsViewModel2
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtExporter
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtImporter
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSyncer
import com.thekeeperofpie.artistalleydatabase.art.search.ArtSearchViewModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseSelectionNavigator
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.utils.Exporter
import com.thekeeperofpie.artistalleydatabase.utils.Importer
import com.thekeeperofpie.artistalleydatabase.utils_room.DatabaseSyncer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

interface ArtEntryComponent {

    val artSearchViewModel: Provider<ArtSearchViewModel>
    val artBrowseSelectionViewModel: Provider<ArtBrowseSelectionViewModel>
    val artEntryDetailsViewModel: Provider<ArtEntryDetailsViewModel>
    val artEntryDetailsViewModel2Factory: ArtEntryDetailsViewModel2.Factory

    @SingleIn(AppScope::class)
    @Provides
    fun provideArtEntryDao(database: ArtEntryDatabase): ArtEntryDao = database.artEntryDao()

    @SingleIn(AppScope::class)
    @Provides
    fun provideArtEntryDetailsDao(database: ArtEntryDatabase): ArtEntryDetailsDao =
        database.artEntryDetailsDao()

    @SingleIn(AppScope::class)
    @Provides
    fun provideArtEntryBrowseDao(database: ArtEntryDatabase): ArtEntryBrowseDao =
        database.artEntryBrowseDao()

    @SingleIn(AppScope::class)
    @Provides
    fun provideArtEntrySyncDao(database: ArtEntryDatabase): ArtEntrySyncDao =
        database.artEntrySyncDao()

    @IntoSet
    @Provides
    fun provideArtExporter(artExporter: ArtExporter): Exporter = artExporter

    @IntoSet
    @Provides
    fun bindArtImporter(artImporter: ArtImporter): Importer = artImporter

    @IntoSet
    @Provides
    fun provideArtBrowseArtists(artBrowseTabArtists: ArtBrowseTabArtists): BrowseTabViewModel =
        artBrowseTabArtists

    @IntoSet
    @Provides
    fun provideArtBrowseTabCharacters(artBrowseTabCharacters: ArtBrowseTabCharacters): BrowseTabViewModel =
        artBrowseTabCharacters

    @IntoSet
    @Provides
    fun provideArtBrowseTabSeries(artBrowseTabSeries: ArtBrowseTabSeries): BrowseTabViewModel =
        artBrowseTabSeries

    @IntoSet
    @Provides
    fun provideArtBrowseTabTags(artBrowseTabTags: ArtBrowseTabTags): BrowseTabViewModel =
        artBrowseTabTags

    @IntoSet
    @Provides
    fun bindArtSyncer(artSyncer: ArtSyncer): DatabaseSyncer = artSyncer

    @IntoSet
    @Provides
    fun bindArtEntryNavigatorAsBrowseSelectionNavigator(
        artEntryNavigator: ArtEntryNavigator,
    ): BrowseSelectionNavigator = artEntryNavigator
}
