package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.room.Room
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@SingletonScope
@Component
abstract class ArtistAlleyAndroidComponent(
    @get:Provides val application: Application,
    @get:Provides val appScope: ApplicationScope,
) : ArtistAlleyAppComponent {

    val ArtistAlleyAppSettings.bindAniListSettings: AniListSettings
        @Provides get() = this

    val ArtistAlleyAppSettings.bindArtistAlleySettings: ArtistAlleySettings
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
            .addMigrations(ArtistAlleyAppDatabase.Version_6_7, ArtistAlleyAppDatabase.Version_7_8)
            .build()

    @SingletonScope
    @Provides
    fun provideMasterKey(application: Application) = masterKey(application)

    private fun masterKey(application: Application): MasterKey {
        val masterKeyAlias = "${application.packageName}.ArtistAlleyMasterKey"
        return MasterKey.Builder(application, masterKeyAlias)
            .setKeyGenParameterSpec(
                KeyGenParameterSpec.Builder(
                    masterKeyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            .setRequestStrongBoxBacked(true)
            .build()
    }
}
