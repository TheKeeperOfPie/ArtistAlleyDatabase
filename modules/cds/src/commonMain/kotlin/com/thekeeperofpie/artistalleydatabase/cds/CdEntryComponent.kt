package com.thekeeperofpie.artistalleydatabase.cds

import com.thekeeperofpie.artistalleydatabase.browse.BrowseSelectionNavigator
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.cds.browse.CdBrowseTabMusicalArtists
import com.thekeeperofpie.artistalleydatabase.cds.browse.selection.CdBrowseSelectionViewModel
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.cds.persistence.CdExporter
import com.thekeeperofpie.artistalleydatabase.cds.persistence.CdImporter
import com.thekeeperofpie.artistalleydatabase.cds.search.CdSearchViewModel
import com.thekeeperofpie.artistalleydatabase.utils.Exporter
import com.thekeeperofpie.artistalleydatabase.utils.Importer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

interface CdEntryComponent {

    val cdSearchViewModel: Provider<CdSearchViewModel>
    val cdBrowseSelectionViewModel: Provider<CdBrowseSelectionViewModel>
    val cdEntryDetailsViewModel: Provider<CdEntryDetailsViewModel>
    val cdsFromMediaViewModelFactory: CdsFromMediaViewModel.Factory

    @SingleIn(AppScope::class)
    @Provides
    fun provideCdEntryDao(database: CdEntryDatabase): CdEntryDao = database.cdEntryDao()

    @SingleIn(AppScope::class)
    @Provides
    fun provideCdEntryBrowseDao(database: CdEntryDatabase): CdEntryBrowseDao =
        database.cdEntryBrowseDao()

    @SingleIn(AppScope::class)
    @Provides
    fun provideCdEntryDetailsDao(database: CdEntryDatabase): CdEntryDetailsDao =
        database.cdEntryDetailsDao()

    @IntoSet
    @Binds
    val CdExporter.bindExporter: Exporter

    @IntoSet
    @Binds
    val CdImporter.bindImporter: Importer

    @IntoSet
    @Binds
    val CdEntryNavigator.bindBrowseSelectionNavigator: BrowseSelectionNavigator

    @IntoSet
    @Binds
    val CdBrowseTabMusicalArtists.bindBrowseTabViewModel: BrowseTabViewModel
}
