package com.thekeeperofpie.artistalleydatabase.network_utils

import android.app.Application
import com.google.net.cronet.okhttptransport.CronetInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.chromium.net.CronetEngine
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkUtilsHiltModule {

    @Singleton
    @Provides
    fun provideCronetEngine(application: Application) = CronetEngine.Builder(application)
        .setStoragePath(File(application.cacheDir, "cronet").apply { mkdir() }.absolutePath)
        .enableHttpCache(CronetEngine.Builder.HTTP_CACHE_DISK, 500 * 1024 * 1024)
        .enableBrotli(true)
        .build()

    @Singleton
    @Provides
    fun provideOkHttpClient(
        application: Application,
        networkSettings: NetworkSettings,
        cronetEngine: CronetEngine,
        authProviders: @JvmSuppressWildcards Map<String, NetworkAuthProvider>,
    ) =
        OkHttpClient.Builder()
            .cache(
                Cache(
                    directory = File(application.cacheDir, "okhttp"),
                    maxSize = 100L * 1024L * 1024L // 100 MiB
                )
            )
            .addInterceptor(Interceptor { chain ->
                val request = chain.request()
                val authHeader = authProviders[request.url.host]?.authHeader
                if (authHeader == null) {
                    chain.proceed(request)
                } else {
                    request.newBuilder()
                        .addHeader("Authorization", authHeader)
                        .build()
                        .let(chain::proceed)
                }
            })
            .addLoggingInterceptors("Network", networkSettings)
            .addInterceptor(CronetInterceptor.newBuilder(cronetEngine).build())
            .build()
}
