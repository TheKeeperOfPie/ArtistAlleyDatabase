@file:OptIn(InternalCoilApi::class, ExperimentalCoilApi::class)

package com.thekeeperofpie.artistalleydatabase.utils_compose.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.Uri
import coil3.annotation.ExperimentalCoilApi
import coil3.annotation.InternalCoilApi
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.disk.DiskCache
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.network.CacheNetworkResponse
import coil3.network.CacheStrategy
import coil3.network.ConnectivityChecker
import coil3.network.HttpException
import coil3.network.NetworkClient
import coil3.network.NetworkFetcher
import coil3.network.NetworkRequest
import coil3.network.NetworkResponse
import coil3.network.NetworkResponseBody
import coil3.network.httpBody
import coil3.network.httpHeaders
import coil3.network.httpMethod
import coil3.network.ktor3.asNetworkClient
import coil3.request.Options
import coil3.toUri
import coil3.util.MimeTypeMap
import com.thekeeperofpie.artistalleydatabase.utils.ImageWithDimensions
import io.ktor.client.HttpClient
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import okio.Buffer
import okio.FileSystem
import okio.IOException

/** Copied from Coil's [NetworkFetcher] to handle [ImageWithDimensions] directly */
class ImageWithDimensionsFetcher(
    private val url: String,
    private val width: Int?,
    private val height: Int?,
    private val options: Options,
    private val networkClient: Lazy<NetworkClient>,
    private val diskCache: Lazy<DiskCache?>,
    private val cacheStrategy: Lazy<CacheStrategy>,
    private val connectivityChecker: ConnectivityChecker,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        var snapshot = readFromDiskCache()
        try {
            // Fast path: fetch the image from the disk cache without performing a network request.
            var readResult: CacheStrategy.ReadResult? = null
            var cacheResponse: NetworkResponse? = null
            if (snapshot != null) {
                // Always return files with empty metadata as it's likely they've been written
                // to the disk cache manually.
                if (fileSystem.metadata(snapshot.metadata).size == 0L) {
                    return SourceFetchResult(
                        source = snapshot.toImageSource(),
                        mimeType = getMimeType(url, null),
                        dataSource = DataSource.DISK,
                    )
                }

                // Return the image from the disk cache if the cache strategy agrees.
                cacheResponse = snapshot.toNetworkResponseOrNull()
                if (cacheResponse != null) {
                    readResult = cacheStrategy.value.read(cacheResponse, newRequest(), options)
                    val response = readResult.response
                    if (response != null) {
                        return SourceFetchResult(
                            source = snapshot.toImageSource(),
                            mimeType = getMimeType(url, response.headers[CONTENT_TYPE]),
                            dataSource = DataSource.DISK,
                        )
                    }
                }
            }

            // Slow path: fetch the image from the network.
            val networkRequest = readResult?.request ?: newRequest()
            var fetchResult = executeNetworkRequest(networkRequest) { response ->
                // Write the response to the disk cache then open a new snapshot.
                snapshot = writeToDiskCache(snapshot, cacheResponse, networkRequest, response)
                if (snapshot != null) {
                    cacheResponse = snapshot!!.toNetworkResponseOrNull()
                    return@executeNetworkRequest SourceFetchResult(
                        source = snapshot!!.toImageSource(),
                        mimeType = getMimeType(url, cacheResponse?.headers?.get(CONTENT_TYPE)),
                        dataSource = DataSource.NETWORK,
                    )
                }

                // If we failed to read a new snapshot then read the response body if it's not empty.
                val responseBody = response.requireBody().readBuffer()
                if (responseBody.size > 0) {
                    return@executeNetworkRequest SourceFetchResult(
                        source = responseBody.toImageSource(),
                        mimeType = getMimeType(url, response.headers[CONTENT_TYPE]),
                        dataSource = DataSource.NETWORK,
                    )
                }

                return@executeNetworkRequest null
            }

            // Fallback: if the response body is empty, execute a new network request without the
            // cache headers.
            if (fetchResult == null) {
                fetchResult = executeNetworkRequest(newRequest()) { response ->
                    SourceFetchResult(
                        source = response.requireBody().toImageSource(),
                        mimeType = getMimeType(url, response.headers[CONTENT_TYPE]),
                        dataSource = DataSource.NETWORK,
                    )
                }
            }

            return fetchResult
        } catch (e: Exception) {
            snapshot?.closeQuietly()
            throw e
        }
    }

    private fun readFromDiskCache(): DiskCache.Snapshot? {
        if (options.diskCachePolicy.readEnabled) {
            return diskCache.value?.openSnapshot(diskCacheKey)
        } else {
            return null
        }
    }

    private suspend fun writeToDiskCache(
        snapshot: DiskCache.Snapshot?,
        cacheResponse: NetworkResponse?,
        networkRequest: NetworkRequest,
        networkResponse: NetworkResponse,
    ): DiskCache.Snapshot? {
        // Short circuit if we're not allowed to cache this response.
        if (!options.diskCachePolicy.writeEnabled) {
            snapshot?.closeQuietly()
            return null
        }

        val writeResult = cacheStrategy.value.write(cacheResponse, networkRequest, networkResponse, options)
        val modifiedNetworkResponse = writeResult.response ?: return null

        // Open a new editor. Return null if we're unable to write to this entry.
        val editor = if (snapshot != null) {
            snapshot.closeAndOpenEditor()
        } else {
            diskCache.value?.openEditor(diskCacheKey)
        } ?: return null

        // Write the network request metadata and the network response body to disk.
        try {
            fileSystem.write(editor.metadata) {
                CacheNetworkResponse.writeTo(modifiedNetworkResponse, this)
            }
            modifiedNetworkResponse.body?.writeTo(fileSystem, editor.data)
            return editor.commitAndOpenSnapshot()
        } catch (e: Exception) {
            editor.abortQuietly()
            networkResponse.body?.closeQuietly()
            modifiedNetworkResponse.body?.closeQuietly()
            throw e
        }
    }

    private fun newRequest(): NetworkRequest {
        val headers = options.httpHeaders.newBuilder()
        val diskRead = options.diskCachePolicy.readEnabled
        val networkRead = options.networkCachePolicy.readEnabled && connectivityChecker.isOnline()
        when {
            !networkRead && diskRead -> {
                headers[CACHE_CONTROL] = "only-if-cached, max-stale=2147483647"
            }
            networkRead && !diskRead -> if (options.diskCachePolicy.writeEnabled) {
                headers[CACHE_CONTROL] = "no-cache"
            } else {
                headers[CACHE_CONTROL] = "no-cache, no-store"
            }
            !networkRead && !diskRead -> {
                // This causes the request to fail with a 504 Unsatisfiable Request.
                headers[CACHE_CONTROL] = "no-cache, only-if-cached"
            }
        }

        return NetworkRequest(
            url = url,
            method = options.httpMethod,
            headers = headers.build(),
            body = options.httpBody,
            extras = options.extras,
        )
    }

    private suspend fun <T> executeNetworkRequest(
        request: NetworkRequest,
        block: suspend (NetworkResponse) -> T,
    ): T {
        // Prevent executing requests on the main thread that could block due to a
        // networking operation.
        if (options.networkCachePolicy.readEnabled) {
            assertNotOnMainThread()
        }

        return networkClient.value.executeRequest(request) { response ->
            if (response.code !in 200 until 300 && response.code != HTTP_RESPONSE_NOT_MODIFIED) {
                throw HttpException(response)
            }
            block(response)
        }
    }

    /**
     * Parse the response's `content-type` header.
     *
     * "text/plain" is often used as a default/fallback MIME type.
     * Attempt to guess a better MIME type from the file extension.
     */
    @InternalCoilApi
    fun getMimeType(url: String, contentType: String?): String? {
        if (contentType == null || contentType.startsWith(MIME_TYPE_TEXT_PLAIN)) {
            MimeTypeMap.getMimeTypeFromUrl(url)?.let { return it }
        }
        return contentType?.substringBefore(';')
    }

    private fun DiskCache.Snapshot.toNetworkResponseOrNull(): NetworkResponse? {
        try {
            return fileSystem.read(metadata) {
                CacheNetworkResponse.readFrom(this)
            }
        } catch (_: IOException) {
            // If we can't parse the metadata, ignore this entry.
            return null
        }
    }

    private fun DiskCache.Snapshot.toImageSource(): ImageSource {
        return ImageSource(
            file = data,
            fileSystem = fileSystem,
            diskCacheKey = diskCacheKey,
            closeable = this,
            metadata = if (width != null && height != null) {
                ImageMetadata(width, height)
            } else {
                null
            },
        )
    }

    private suspend fun NetworkResponseBody.toImageSource(): ImageSource {
        val buffer = Buffer()
        writeTo(buffer)
        return buffer.toImageSource()
    }

    private fun Buffer.toImageSource(): ImageSource {
        return ImageSource(
            source = this,
            fileSystem = fileSystem,
            metadata = if (width != null && height != null) {
                ImageMetadata(width, height)
            } else {
                null
            },
        )
    }

    private val diskCacheKey: String
        get() = options.diskCacheKey ?: url

    private val fileSystem: FileSystem
        get() = diskCache.value?.fileSystem ?: options.fileSystem

    data class ImageMetadata(val width: Int, val height: Int) : ImageSource.Metadata()

    class Factory(
        networkClient: () -> NetworkClient = { HttpClient().asNetworkClient() },
        cacheStrategy: () -> CacheStrategy = { CacheStrategy.Companion.DEFAULT },
        connectivityChecker: (PlatformContext) -> ConnectivityChecker = ::ConnectivityChecker,
    ) : Fetcher.Factory<Uri> {
        private val networkClientLazy = lazy(networkClient)
        private val cacheStrategyLazy = lazy(cacheStrategy)
        private val connectivityCheckerLazy = singleParameterLazy(connectivityChecker)

        override fun create(
            data: Uri,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher? {
            if (!isApplicable(data)) return null
            return NetworkFetcher(
                url = data.toString(),
                options = options,
                networkClient = networkClientLazy,
                diskCache = lazy { imageLoader.diskCache },
                cacheStrategy = cacheStrategyLazy,
                connectivityChecker = connectivityCheckerLazy.get(options.context),
            )
        }

        fun create(
            data: Uri,
            width: Int?,
            height: Int?,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher? {
            if (!isApplicable(data)) return null
            return ImageWithDimensionsFetcher(
                url = data.toString(),
                width = width,
                height = height,
                options = options,
                networkClient = networkClientLazy,
                diskCache = lazy { imageLoader.diskCache },
                cacheStrategy = cacheStrategyLazy,
                connectivityChecker = connectivityCheckerLazy.get(options.context),
            )
        }

        private fun isApplicable(data: Uri): Boolean {
            return data.scheme == "http" || data.scheme == "https"
        }
    }

    companion object {
        private val imageWithDimensionsFactory = Factory()
        val factory = object : Fetcher.Factory<ImageWithDimensions> {
            override fun create(
                data: ImageWithDimensions,
                options: Options,
                imageLoader: ImageLoader,
            ): Fetcher? {
                val uri = data.coilImageModel as? com.eygraber.uri.Uri ?: return null
                return Fetcher {
                    imageWithDimensionsFactory.create(
                        data = uri.toString().toUri(),
                        width = data.width,
                        height = data.height,
                        options = options,
                        imageLoader = imageLoader,
                    )?.fetch()
                }
            }

        }
    }
}


internal fun DiskCache.Editor.abortQuietly() {
    try {
        abort()
    } catch (_: Exception) {}
}

internal fun AutoCloseable.closeQuietly() {
    try {
        close()
    } catch (e: RuntimeException) {
        throw e
    } catch (_: Exception) {}
}

internal suspend fun NetworkResponseBody.readBuffer(): Buffer = use { body ->
    val buffer = Buffer()
    body.writeTo(buffer)
    return buffer
}

internal fun NetworkResponse.requireBody(): NetworkResponseBody {
    return checkNotNull(body) { "body == null" }
}

internal fun assertNotOnMainThread() {}

internal fun <P, T> singleParameterLazy(initializer: (P) -> T) = SingleParameterLazy(initializer)

internal class SingleParameterLazy<P, T>(initializer: (P) -> T) : SynchronizedObject() {
    private var initializer: ((P) -> T)? = initializer
    private var _value: Any? = UNINITIALIZED

    @Suppress("UNCHECKED_CAST")
    fun get(parameter: P): T {
        val value1 = _value
        if (value1 !== UNINITIALIZED) {
            return value1 as T
        }

        return synchronized(this) {
            val value2 = _value
            if (value2 !== UNINITIALIZED) {
                value2 as T
            } else {
                val newValue = initializer!!(parameter)
                _value = newValue
                initializer = null
                newValue
            }
        }
    }
}

private object UNINITIALIZED

internal const val CACHE_CONTROL = "Cache-Control"
internal const val CONTENT_TYPE = "Content-Type"
internal const val HTTP_METHOD_GET = "GET"
internal const val MIME_TYPE_TEXT_PLAIN = "text/plain"
internal const val HTTP_RESPONSE_OK = 200
internal const val HTTP_RESPONSE_NOT_MODIFIED = 304
