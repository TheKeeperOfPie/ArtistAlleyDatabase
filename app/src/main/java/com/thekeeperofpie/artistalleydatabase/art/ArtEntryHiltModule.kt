package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.AppDatabase
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.export.Exporter
import com.thekeeperofpie.artistalleydatabase.android_utils.importer.Importer
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDataConverter
import com.thekeeperofpie.artistalleydatabase.art.export.ArtExporter
import com.thekeeperofpie.artistalleydatabase.art.importer.ArtImporter
import com.thekeeperofpie.artistalleydatabase.art.json.ArtJson
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
object ArtEntryHiltModule {

    @Provides
    fun provideArtJson(appJson: AppJson) = ArtJson(appJson.json)

    @Provides
    fun provideArtEntryDao(appDatabase: AppDatabase) = appDatabase.artEntryDao()

    @Provides
    fun provideArtEntryEditDao(appDatabase: AppDatabase) = appDatabase.artEntryEditDao()

    @Provides
    fun provideArtEntryDetailsDao(appDatabase: AppDatabase) = appDatabase.artEntryDetailsDao()

    @Provides
    fun provideArtEntryBrowseDao(appDatabase: AppDatabase) = appDatabase.artEntryBrowseDao()

    @Provides
    fun provideArtEntryAdvancedSearchDao(appDatabase: AppDatabase) =
        appDatabase.artEntryAdvancedSearchDao()

    @IntoSet
    @Provides
    fun provideArtExporter(
        application: Application,
        artEntryDao: ArtEntryDao,
        artEntryDataConverter: ArtEntryDataConverter,
        appJson: AppJson
    ): Exporter = ArtExporter(
        appContext = application,
        artEntryDao = artEntryDao,
        artEntryDataConverter = artEntryDataConverter,
        appJson = appJson
    )

    @IntoSet
    @Provides
    fun provideArtImporter(
        application: Application,
        artEntryDao: ArtEntryDao,
        appMoshi: AppMoshi,
    ): Importer = ArtImporter(
        appContext = application,
        artEntryDao = artEntryDao,
        moshi = appMoshi.moshi,
    )
}