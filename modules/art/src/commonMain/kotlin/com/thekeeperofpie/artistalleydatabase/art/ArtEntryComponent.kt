package com.thekeeperofpie.artistalleydatabase.art

import com.thekeeperofpie.artistalleydatabase.art.browse.ArtBrowseTabArtists
import com.thekeeperofpie.artistalleydatabase.art.browse.ArtBrowseTabCharacters
import com.thekeeperofpie.artistalleydatabase.art.browse.ArtBrowseTabSeries
import com.thekeeperofpie.artistalleydatabase.art.browse.ArtBrowseTabTags
import com.thekeeperofpie.artistalleydatabase.art.browse.selection.ArtBrowseSelectionViewModel
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDatabase
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsViewModel2
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtExporter
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtImporter
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSyncer
import com.thekeeperofpie.artistalleydatabase.art.search.ArtSearchViewModel
import com.thekeeperofpie.artistalleydatabase.browse.BrowseSelectionNavigator
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.Exporter
import com.thekeeperofpie.artistalleydatabase.utils.Importer
import com.thekeeperofpie.artistalleydatabase.utils_room.DatabaseSyncer
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface ArtEntryComponent {

    val artSearchViewModel: () -> ArtSearchViewModel
    val artBrowseSelectionViewModel: () -> ArtBrowseSelectionViewModel
    val artEntryDetailsViewModel: () -> ArtEntryDetailsViewModel
    val artEntryDetailsViewModel2: () -> ArtEntryDetailsViewModel2

    @SingletonScope
    @Provides
    fun provideArtEntryDao(database: ArtEntryDatabase) = database.artEntryDao()

    @SingletonScope
    @Provides
    fun provideArtEntryDetailsDao(database: ArtEntryDatabase) = database.artEntryDetailsDao()

    @SingletonScope
    @Provides
    fun provideArtEntryBrowseDao(database: ArtEntryDatabase) = database.artEntryBrowseDao()

    @SingletonScope
    @Provides
    fun provideArtEntrySyncDao(database: ArtEntryDatabase) = database.artEntrySyncDao()

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
