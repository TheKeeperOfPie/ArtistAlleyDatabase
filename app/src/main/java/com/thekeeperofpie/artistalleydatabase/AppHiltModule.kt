package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import androidx.room.Room
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.anichive.BuildConfig
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDatabase
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDatabase
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDatabase
import com.thekeeperofpie.artistalleydatabase.utils.CryptoUtils
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppMetadataProvider
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppHiltModule {

    @Singleton
    @Provides
    fun provideMasterKey(application: Application) = CryptoUtils.masterKey(application)

    @Singleton
    @Provides
    fun provideCustomApplication(application: Application) =
        application as CustomApplication

    @Singleton
    @Provides
    fun provideApplicationScope(application: Application): ApplicationScope =
        (application as CustomApplication).scope

    @Singleton
    @Provides
    fun provideAppFileSystem(application: Application, masterKey: MasterKey) =
        AppFileSystem(application, masterKey)

    @Singleton
    @Provides
    fun provideAppDatabase(application: Application) =
        Room.databaseBuilder(application, AppDatabase::class.java, "appDatabase")
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .addMigrations(Migration_6_7)
            .build()

    @Singleton
    @Provides
    fun provideAniListDatabase(appDatabase: AppDatabase): AniListDatabase = appDatabase

    @Singleton
    @Provides
    fun provideAnimeDatabase(appDatabase: AppDatabase): AnimeDatabase = appDatabase

    @Singleton
    @Provides
    fun provideArtEntryDatabase(appDatabase: AppDatabase): ArtEntryDatabase = appDatabase

    @Singleton
    @Provides
    fun provideCdEntryDatabase(appDatabase: AppDatabase): CdEntryDatabase = appDatabase

    @Singleton
    @Provides
    fun provideMusicalArtistDatabase(appDatabase: AppDatabase): MusicalArtistDatabase = appDatabase

    @Singleton
    @Provides
    fun provideVgmdbDatabase(appDatabase: AppDatabase): VgmdbDatabase = appDatabase

    @Singleton
    @Provides
    fun provideAniListJson(json: Json) = AniListJson(json)

    @Singleton
    @Provides
    fun provideVgmdbJson(json: Json) = VgmdbJson(json)

    @Singleton
    @Provides
    fun provideFeatureOverrideProvider(): FeatureOverrideProvider = AppFeatureOverrideProvider()

    @Singleton
    @Provides
    fun provideAppMetadataProvider(): AppMetadataProvider = object : AppMetadataProvider {
        override val versionCode = BuildConfig.VERSION_CODE
        override val versionName = BuildConfig.VERSION_NAME
        override val appDrawableModel = R.mipmap.ic_launcher
    }
}
