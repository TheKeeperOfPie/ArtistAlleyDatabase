package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.CryptoUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.utils_network.buildNetworkClient
import kotlinx.coroutines.flow.MutableStateFlow
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

    @SingletonScope
    @Provides
    fun provideNetworkClient(): NetworkClient = buildNetworkClient(
        scope = appScope,
        application = application,
        networkSettings = object : NetworkSettings {
            override val networkLoggingLevel =
                MutableStateFlow(NetworkSettings.NetworkLoggingLevel.NONE)
            override val enableNetworkCaching = MutableStateFlow(false)
        },
        authProviders = emptyMap(),
    )
}
