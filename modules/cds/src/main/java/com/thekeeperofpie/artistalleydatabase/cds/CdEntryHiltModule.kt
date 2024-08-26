package com.thekeeperofpie.artistalleydatabase.cds

import com.thekeeperofpie.artistalleydatabase.browse.BrowseSelectionNavigator
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.cds.browse.CdBrowseTabMusicalArtists
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.cds.persistence.CdExporter
import com.thekeeperofpie.artistalleydatabase.cds.persistence.CdImporter
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDao
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import com.thekeeperofpie.artistalleydatabase.utils_room.Exporter
import com.thekeeperofpie.artistalleydatabase.utils_room.Importer
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CdEntryHiltModule {

    @Singleton
    @Provides
    fun provideCdEntryDao(database: CdEntryDatabase) = database.cdEntryDao()

    @Singleton
    @Provides
    fun provideCdEntryBrowseDao(database: CdEntryDatabase) = database.cdEntryBrowseDao()

    @Singleton
    @Provides
    fun provideCdEntryDetailsDao(database: CdEntryDatabase) = database.cdEntryDetailsDao()

    @IntoSet
    @Singleton
    @Provides
    fun provideCdExporter(
        appFileSystem: AppFileSystem,
        cdEntryDao: CdEntryDao,
        dataConverter: DataConverter,
        vgmdbDataConverter: VgmdbDataConverter,
        appJson: AppJson
    ): Exporter = CdExporter(
        appFileSystem = appFileSystem,
        cdEntryDao = cdEntryDao,
        dataConverter = dataConverter,
        vgmdbDataConverter = vgmdbDataConverter,
        appJson = appJson,
    )

    @IntoSet
    @Singleton
    @Provides
    fun provideCdImporter(
        appFileSystem: AppFileSystem,
        cdEntryDao: CdEntryDao,
        appJson: AppJson,
    ): Importer = CdImporter(
        appFileSystem = appFileSystem,
        cdEntryDao = cdEntryDao,
        appJson = appJson,
    )

    @Singleton
    @Provides
    fun provideCdEntryNavigator() = CdEntryNavigator()

    @IntoSet
    @Singleton
    @Provides
    fun bindCdEntryNavigatorAsBrowseSelectionNavigator(
        cdEntryNavigator: CdEntryNavigator
    ): BrowseSelectionNavigator = cdEntryNavigator

    @IntoSet
    @Singleton
    @Provides
    fun provideCdBrowseTabPerformers(
        musicalArtistDao: MusicalArtistDao,
        vgmdbArtistDao: VgmdbArtistDao,
        cdEntryNavigator: CdEntryNavigator,
    ): BrowseTabViewModel = CdBrowseTabMusicalArtists(
        musicalArtistDao,
        vgmdbArtistDao,
        cdEntryNavigator,
    )
}
