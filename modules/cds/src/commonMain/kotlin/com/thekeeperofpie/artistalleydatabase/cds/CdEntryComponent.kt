package com.thekeeperofpie.artistalleydatabase.cds

import com.thekeeperofpie.artistalleydatabase.browse.BrowseSelectionNavigator
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.cds.browse.CdBrowseTabMusicalArtists
import com.thekeeperofpie.artistalleydatabase.cds.browse.selection.CdBrowseSelectionViewModel
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.cds.persistence.CdExporter
import com.thekeeperofpie.artistalleydatabase.cds.persistence.CdImporter
import com.thekeeperofpie.artistalleydatabase.cds.search.CdSearchViewModel
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils_room.Exporter
import com.thekeeperofpie.artistalleydatabase.utils_room.Importer
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface CdEntryComponent {

    val cdSearchViewModel: () -> CdSearchViewModel
    val cdBrowseSelectionViewModel: () -> CdBrowseSelectionViewModel
    val cdEntryDetailsViewModel: () -> CdEntryDetailsViewModel
    val cdsFromMediaViewModel: (mediaId: String) -> CdsFromMediaViewModel

    @SingletonScope
    @Provides
    fun provideCdEntryDao(database: CdEntryDatabase) = database.cdEntryDao()

    @SingletonScope
    @Provides
    fun provideCdEntryBrowseDao(database: CdEntryDatabase) = database.cdEntryBrowseDao()

    @SingletonScope
    @Provides
    fun provideCdEntryDetailsDao(database: CdEntryDatabase) = database.cdEntryDetailsDao()

    @IntoSet
    @SingletonScope
    @Provides
    fun provideCdExporter(cdExporter: CdExporter): Exporter = cdExporter

    @IntoSet
    @SingletonScope
    @Provides
    fun provideCdImporter(cdImporter: CdImporter): Importer = cdImporter

    @IntoSet
    @SingletonScope
    @Provides
    fun bindCdEntryNavigatorAsBrowseSelectionNavigator(
        cdEntryNavigator: CdEntryNavigator
    ): BrowseSelectionNavigator = cdEntryNavigator

    @IntoSet
    @SingletonScope
    @Provides
    fun provideCdBrowseTabPerformers(
        cdBrowseTabMusicalArtists: CdBrowseTabMusicalArtists,
    ): BrowseTabViewModel = cdBrowseTabMusicalArtists
}
