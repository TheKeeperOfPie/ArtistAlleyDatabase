package com.thekeeperofpie.artistalleydatabase.utils.io

import android.app.Application
import android.content.ContentResolver
import android.graphics.BitmapFactory
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import co.touchlab.kermit.Logger
import com.eygraber.uri.Uri
import com.eygraber.uri.toAndroidUri
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import kotlinx.datetime.Instant
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.asInputStream
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import me.tatarka.inject.annotations.Inject
import java.io.File
import java.net.URL

@SingletonScope
@Inject
actual class AppFileSystem(private val application: Application, private val masterKey: MasterKey) {

    companion object {
        private const val TAG = "AppFileSystem.android"
    }

    private val BitmapFactory.Options.widthHeight: Pair<Int?, Int?>
        get() {
            val imageWidth = if (outWidth == -1) null else outWidth
            val imageHeight = if (outHeight == -1) null else outHeight
            return (imageWidth to imageHeight)
        }

    actual fun cachePath(path: String) = Path(application.cacheDir.resolve(path).path)
    actual fun filePath(path: String) = Path(application.filesDir.resolve(path).path)
    actual fun openUriSource(uri: Uri): Source? =
        application.contentResolver.openInputStream(uri.toAndroidUri())?.asSource()?.buffered()

    actual fun openUriSink(uri: Uri, mode: String): Sink? =
        application.contentResolver.openOutputStream(uri.toAndroidUri(), mode)?.asSink()?.buffered()

    actual fun getImageWidthHeight(uri: Uri): Pair<Int?, Int?> {
        val options = BitmapFactory.Options().apply {
            this.inJustDecodeBounds = true
        }
        try {
            when {
                uri.scheme == "http" || uri.scheme == "https" -> URL(uri.toString()).openStream()
                uri.scheme == "file"
                        && uri.authority?.isEmpty() == true
                        && uri.path?.startsWith("/android_asset") == true ->
                    application.assets.open(uri.path!!.removePrefix("/android_asset/"))
                else -> openUriSource(uri)?.asInputStream()
            }.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: Exception) {
            Logger.d(TAG, e) {
                "Error loading image size for scheme = ${uri.scheme}, " +
                        "authority = ${uri.authority}, path = ${uri.path}, uri = $uri"
            }
            return null to null
        }

        return options.widthHeight
    }

    actual fun getImageType(path: Path): String? {
        val options = BitmapFactory.Options().apply {
            this.inJustDecodeBounds = true
        }
        try {
            if (!SystemFileSystem.exists(path)) return null
            SystemFileSystem.source(path).buffered().asInputStream().use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (ignored: Exception) {
            return null
        }

        return options.outMimeType
    }

    actual fun writeEntryImage(
        outputPath: Path,
        imageUri: Uri?,
    ): Result<*>? {
        imageUri ?: return null
        val imageSource = try {
            when (imageUri.scheme) {
                "http", "https" -> URL(imageUri.toString()).openStream().asSource()
                ContentResolver.SCHEME_FILE -> {
                    if (imageUri.path == outputPath.toUri().path) {
                        return null
                    } else openUriSource(imageUri)
                }
                else -> openUriSource(imageUri)
            }
        } catch (e: Exception) {
            return Result.failure<Any>(e)
        } ?: run {
            return Result.failure<Any>(IllegalArgumentException("Fail to read image"))
        }

        outputPath.parent?.let { SystemFileSystem.createDirectories(it) }

        SystemFileSystem.sink(outputPath).use { output ->
            imageSource.buffered().use { input ->
                input.transferTo(output)
            }
        }
        return null
    }

    actual fun openEncryptedSource(path: Path) =
        EncryptedFile.Builder(
            application,
            File(path.toString()),
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
            .openFileInput()
            .asSource()
            .buffered()

    actual fun openEncryptedSink(path: Path) =
        EncryptedFile.Builder(
            application,
            File(path.toString()),
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
            .openFileOutput()
            .asSink()
            .buffered()

    actual fun exists(path: Path) = SystemFileSystem.exists(path)

    actual fun createDirectories(path: Path) =
        SystemFileSystem.createDirectories(path, mustCreate = false)

    actual fun delete(path: Path) = SystemFileSystem.delete(path, mustExist = false)

    actual fun metadataOrNull(path: Path) = SystemFileSystem.metadataOrNull(path)

    actual fun sink(path: Path) = SystemFileSystem.sink(path)

    actual fun list(path: Path) = SystemFileSystem.list(path)

    actual fun atomicMove(source: Path, destination: Path) =
        SystemFileSystem.atomicMove(source, destination)

    actual fun source(path: Path) = SystemFileSystem.source(path)

    // TODO: Doesn't handle multiplatform
    actual fun lastModifiedTime(path: Path) =
        Instant.fromEpochMilliseconds(File(path.toString()).lastModified())
}
