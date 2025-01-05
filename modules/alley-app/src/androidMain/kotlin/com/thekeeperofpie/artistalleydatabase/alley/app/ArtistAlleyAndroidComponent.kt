package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
import androidx.room.Room
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.CryptoUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@SingletonScope
@Component
abstract class ArtistAlleyAndroidComponent(
    @get:Provides val application: Application,
    @get:Provides val appScope: ApplicationScope,
) : ArtistAlleyAppComponent {

    val ArtistAlleyAppSettings.bindArtistAlleySettings: ArtistAlleySettings
        @Provides get() = this

    val ArtistAlleyAppDatabase.bindArtistAlleyDatabase: ArtistAlleyDatabase
        @Provides get() = this

    @SingletonScope
    @Provides
    fun provideArtistAlleyAppDatabase(application: Application) =
        Room.databaseBuilder(
            application,
            ArtistAlleyAppDatabase::class.java,
            "artistAlleyAppDatabase"
        )
            .fallbackToDestructiveMigration(true)
            .addMigrations(
                ArtistAlleyAppDatabase.Version_6_7,
                ArtistAlleyAppDatabase.Version_7_8,
                ArtistAlleyAppDatabase.Version_8_9,
            )
            .build()

    @SingletonScope
    @Provides
    fun provideMasterKey(application: Application) =
        CryptoUtils.masterKey(application, "ArtistAlleyMasterKey")
}
