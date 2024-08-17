package com.thekeeperofpie.artistalleydatabase

import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkAuthProvider
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.utils_network.buildNetworkClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkHiltModule {

    @Singleton
    @Provides
    fun provideNetworkClient(
        scopedApplication: ScopedApplication,
        networkSettings: NetworkSettings,
        authProviders: @JvmSuppressWildcards Map<String, NetworkAuthProvider>,
    ) = buildNetworkClient(
        scope = scopedApplication.scope,
        application = scopedApplication.app,
        networkSettings = networkSettings,
        authProviders = authProviders,
    )

    @Singleton
    @Provides
    fun provideOkHttpClient(networkClient: NetworkClient) = networkClient.okHttpClient

    @Singleton
    @Provides
    fun provideKtorClient(okHttpClient: OkHttpClient) = HttpClient(OkHttp) {
        engine {
            preconfigured = okHttpClient
        }
    }
}
