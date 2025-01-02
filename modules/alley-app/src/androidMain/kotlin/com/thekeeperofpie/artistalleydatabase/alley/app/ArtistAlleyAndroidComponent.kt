package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.navigation.NavType
import androidx.room.Room
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyComponent
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.DataInitializer
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.CustomNavTypes
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import kotlin.reflect.KType

@SingletonScope
@Component
abstract class ArtistAlleyAndroidComponent(
    @get:Provides val application: Application,
    @get:Provides val appScope: ApplicationScope,
) : ArtistAlleyComponent {
    abstract val dataInitializer: DataInitializer
    abstract val navigationTypeMap: NavigationTypeMap

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
    fun provideArtistAlleyDatabase(database: ArtistAlleyAppDatabase): ArtistAlleyDatabase = database

    @SingletonScope
    @Provides
    fun provideArtistAlleySettings(
        artistAlleyAppSettings: ArtistAlleyAppSettings,
    ): ArtistAlleySettings = artistAlleyAppSettings

    @SingletonScope
    @Provides
    fun provideAniListSettings(artistAlleyAppSettings: ArtistAlleyAppSettings): AniListSettings =
        artistAlleyAppSettings

    @SingletonScope
    @Provides
    @IntoSet
    fun provideBaseTypeMap() : Map<KType, NavType<*>> = CustomNavTypes.baseTypeMap

    @SingletonScope
    @Provides
    fun bindsTypeMap(typeMaps: @JvmSuppressWildcards Set<Map<KType, NavType<*>>>): NavigationTypeMap =
        NavigationTypeMap(typeMaps.fold(mapOf<KType, NavType<*>>()) { acc, map -> acc + map })

    @SingletonScope
    @Provides
    fun provideJson() = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

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
