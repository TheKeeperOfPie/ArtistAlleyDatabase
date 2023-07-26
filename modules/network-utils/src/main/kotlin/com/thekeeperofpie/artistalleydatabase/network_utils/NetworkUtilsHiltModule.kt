package com.thekeeperofpie.artistalleydatabase.network_utils

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.net.cronet.okhttptransport.CronetInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.Cache
import okhttp3.Dns
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.chromium.net.CronetEngine
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

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
    ): OkHttpClient {
        val connectivityManager = application.getSystemService(ConnectivityManager::class.java)
        return OkHttpClient.Builder()
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
            .dns { hostname ->
                // Manually timeout DNS because OkHttp doesn't support that by default
                runBlocking {
                    if (!connectivityManager.isConnected()) {
                        throw IOException("No active connection")
                    }
                    withTimeout(5.seconds) {
                        Dns.SYSTEM.lookup(hostname)
                    }
                }
            }
            .addLoggingInterceptors("Network", networkSettings)
            .addInterceptor(CronetInterceptor.newBuilder(cronetEngine).build())
            .addInterceptor { chain ->
                // TODO: Improve this mechanism, move the failure to a lower level?
                if (connectivityManager.isConnected()) {
                    chain.proceed(chain.request())
                } else {
                    chain.withConnectTimeout(1, TimeUnit.SECONDS)
                        .withReadTimeout(1, TimeUnit.SECONDS)
                        .withWriteTimeout(1, TimeUnit.SECONDS)
                        .proceed(chain.request())
                }
            }
            .build()
    }

    private fun ConnectivityManager.isConnected() = activeNetwork
        ?.let { getNetworkCapabilities(it) }
        ?.let {
            it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } == true
}
