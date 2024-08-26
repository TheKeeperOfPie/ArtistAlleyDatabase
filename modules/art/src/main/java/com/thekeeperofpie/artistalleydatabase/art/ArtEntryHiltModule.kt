package com.thekeeperofpie.artistalleydatabase.art

import com.thekeeperofpie.artistalleydatabase.android_utils.persistence.DatabaseSyncer
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.browse.ArtBrowseTabArtists
import com.thekeeperofpie.artistalleydatabase.art.browse.ArtBrowseTabCharacters
import com.thekeeperofpie.artistalleydatabase.art.browse.ArtBrowseTabSeries
import com.thekeeperofpie.artistalleydatabase.art.browse.ArtBrowseTabTags
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDatabase
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntrySyncDao
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtExporter
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtImporter
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSyncer
import com.thekeeperofpie.artistalleydatabase.browse.BrowseSelectionNavigator
import com.thekeeperofpie.artistalleydatabase.browse.BrowseTabViewModel
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import com.thekeeperofpie.artistalleydatabase.utils_room.Exporter
import com.thekeeperofpie.artistalleydatabase.utils_room.Importer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ArtEntryHiltModule {

    @Singleton
    @Provides
    fun provideArtEntryDao(database: ArtEntryDatabase) = database.artEntryDao()

    @Singleton
    @Provides
    fun provideArtEntryDetailsDao(database: ArtEntryDatabase) = database.artEntryDetailsDao()

    @Singleton
    @Provides
    fun provideArtEntryBrowseDao(database: ArtEntryDatabase) = database.artEntryBrowseDao()

    @Singleton
    @Provides
    fun provideArtEntrySyncDao(database: ArtEntryDatabase) = database.artEntrySyncDao()

    @IntoSet
    @Singleton
    @Provides
    fun provideArtExporter(
        appFileSystem: AppFileSystem,
        artEntryDao: ArtEntryDao,
        dataConverter: DataConverter,
        appJson: AppJson
    ): Exporter = ArtExporter(
        appFileSystem = appFileSystem,
        artEntryDao = artEntryDao,
        dataConverter = dataConverter,
        appJson = appJson
    )

    @IntoSet
    @Singleton
    @Provides
    fun provideArtImporter(
        appFileSystem: AppFileSystem,
        artEntryDao: ArtEntryDao,
        appJson: AppJson,
    ): Importer = ArtImporter(
        appFileSystem = appFileSystem,
        artEntryDao = artEntryDao,
        appJson = appJson,
    )

    @IntoSet
    @Singleton
    @Provides
    fun provideArtBrowseArtists(
        appFileSystem: AppFileSystem,
        artEntryBrowseDao: ArtEntryBrowseDao,
        artEntryNavigator: ArtEntryNavigator,
    ): BrowseTabViewModel = ArtBrowseTabArtists(appFileSystem, artEntryBrowseDao, artEntryNavigator)

    @IntoSet
    @Singleton
    @Provides
    fun provideArtBrowseCharacters(
        appFileSystem: AppFileSystem,
        artEntryBrowseDao: ArtEntryBrowseDao,
        artEntryNavigator: ArtEntryNavigator,
        appJson: AppJson,
        characterRepository: CharacterRepository,
    ): BrowseTabViewModel = ArtBrowseTabCharacters(
        appFileSystem,
        artEntryBrowseDao,
        artEntryNavigator,
        appJson,
        characterRepository
    )

    @IntoSet
    @Singleton
    @Provides
    fun provideArtBrowseSeries(
        appFileSystem: AppFileSystem,
        artEntryBrowseDao: ArtEntryBrowseDao,
        artEntryNavigator: ArtEntryNavigator,
        appJson: AppJson,
        mediaRepository: MediaRepository,
    ): BrowseTabViewModel = ArtBrowseTabSeries(
        appFileSystem,
        artEntryBrowseDao,
        artEntryNavigator,
        appJson,
        mediaRepository
    )

    @IntoSet
    @Singleton
    @Provides
    fun provideArtBrowseTags(
        appFileSystem: AppFileSystem,
        artEntryBrowseDao: ArtEntryBrowseDao,
        artEntryNavigator: ArtEntryNavigator,
    ): BrowseTabViewModel = ArtBrowseTabTags(appFileSystem, artEntryBrowseDao, artEntryNavigator)

    @Singleton
    @Provides
    fun provideArtEntryNavigator() = ArtEntryNavigator()

    @IntoSet
    @Singleton
    @Provides
    fun provideArtSyncer(
        appJson: AppJson,
        artEntrySyncDao: ArtEntrySyncDao,
        characterRepository: CharacterRepository,
        characterEntryDao: CharacterEntryDao,
        mediaRepository: MediaRepository,
        mediaEntryDao: MediaEntryDao,
    ): DatabaseSyncer = ArtSyncer(
        appJson,
        artEntrySyncDao,
        characterRepository,
        characterEntryDao,
        mediaRepository,
        mediaEntryDao
    )

    @IntoSet
    @Singleton
    @Provides
    fun bindArtEntryNavigatorAsBrowseSelectionNavigator(
        artEntryNavigator: ArtEntryNavigator
    ): BrowseSelectionNavigator =
        artEntryNavigator
}
