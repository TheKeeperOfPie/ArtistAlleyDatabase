package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
abstract class ArtistAlleyDesktopComponent(
    @get:Provides val scope: ApplicationScope,
) : ArtistAlleyAppComponent {
    abstract val appFileSystem: AppFileSystem

    val ArtistAlleyAppDatabase.bindArtistAlleyDatabase: ArtistAlleyDatabase
        @Provides get() = this

    @SingletonScope
    @Provides
    fun provideArtistAlleyAppDatabase() = Room.inMemoryDatabaseBuilder<ArtistAlleyAppDatabase>()
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .build()

    val ArtistAlleyDesktopSettings.bindArtistAlleySettings: ArtistAlleySettings
        @Provides get() = this
}
