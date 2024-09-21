package com.thekeeperofpie.artistalleydatabase.test_utils

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import me.tatarka.inject.annotations.Provides

interface TestNetworkComponent {

    @SingletonScope
    @Provides
    fun provideOkHttpClient(testNetworkController: TestNetworkController) =
        testNetworkController.okHttpClient

    @SingletonScope
    @Provides
    fun provideKtorClient(testNetworkController: TestNetworkController) = HttpClient(OkHttp) {
        engine {
            preconfigured = testNetworkController.okHttpClient
        }
    }

    @SingletonScope
    @Provides
    fun provideTestNetworkController() = TestNetworkController

    @SingletonScope
    @Provides
    fun provideMasterKey(application: Application): Any = TODO()
}
