package com.thekeeperofpie.artistalleydatabase.cds

import android.app.Application
import com.squareup.moshi.Moshi
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.persistence.Exporter
import com.thekeeperofpie.artistalleydatabase.android_utils.persistence.Importer
import com.thekeeperofpie.artistalleydatabase.browse.BrowseSelectionNavigator
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.cds.browse.CdBrowseTabMusicalArtists
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.cds.persistence.CdExporter
import com.thekeeperofpie.artistalleydatabase.cds.persistence.CdImporter
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.form.EntryNavigator
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
object CdEntryHiltModule {

    @Provides
    fun provideCdEntryDao(database: CdEntryDatabase) = database.cdEntryDao()

    @Provides
    fun provideCdEntryBrowseDao(database: CdEntryDatabase) = database.cdEntryBrowseDao()

    @Provides
    fun provideCdEntryDetailsDao(database: CdEntryDatabase) = database.cdEntryDetailsDao()

    @IntoSet
    @Provides
    fun provideCdExporter(
        application: Application,
        cdEntryDao: CdEntryDao,
        dataConverter: DataConverter,
        vgmdbDataConverter: VgmdbDataConverter,
        appJson: AppJson
    ): Exporter = CdExporter(
        appContext = application,
        cdEntryDao = cdEntryDao,
        dataConverter = dataConverter,
        vgmdbDataConverter = vgmdbDataConverter,
        appJson = appJson
    )

    @IntoSet
    @Provides
    fun provideCdImporter(
        application: Application,
        cdEntryDao: CdEntryDao,
        moshi: Moshi,
    ): Importer = CdImporter(
        appContext = application,
        cdEntryDao = cdEntryDao,
        moshi = moshi,
    )

    @Provides
    fun provideCdEntryNavigator(application: Application) = CdEntryNavigator(application)

    @IntoSet
    @Provides
    fun bindCdEntryNavigatorAsBrowseSelectionNavigator(
        cdEntryNavigator: CdEntryNavigator
    ): BrowseSelectionNavigator = cdEntryNavigator

    @IntoSet
    @Provides
    fun bindCdEntryNavigatorAsEntryNavigator(
        cdEntryNavigator: CdEntryNavigator
    ): EntryNavigator = cdEntryNavigator

    @IntoSet
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