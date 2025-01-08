package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
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

    @SingletonScope
    @Provides
    fun provideMasterKey(application: Application) =
        CryptoUtils.masterKey(application, "ArtistAlleyMasterKey")
}
