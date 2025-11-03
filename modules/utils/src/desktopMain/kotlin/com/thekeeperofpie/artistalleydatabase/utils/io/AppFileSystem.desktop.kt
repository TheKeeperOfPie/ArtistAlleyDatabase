package com.thekeeperofpie.artistalleydatabase.utils.io

import com.eygraber.uri.Uri
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.asInputStream
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.FileMetadata
import kotlinx.io.files.Path
import java.net.URI
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import javax.imageio.ImageIO
import javax.imageio.stream.FileCacheImageInputStream
import kotlin.io.path.pathString
import kotlin.time.toKotlinInstant

@SingleIn(AppScope::class)
@Inject
actual class AppFileSystem {
    private val fileSystem = Jimfs.newFileSystem(Configuration.unix())

    init {
        listOf("/cache", "/files").forEach {
            Files.createDirectories(fileSystem.getPath(it))
        }
    }

    actual fun cachePath(path: String) = Path("/cache/$path")

    actual fun filePath(path: String) = Path("/files/$path")

    actual fun openUriSource(uri: Uri): Source? = if (uri.scheme == "jar") {
        URI.create(uri.toString()).toURL().openStream().asSource().buffered()
    } else {
        Files.newInputStream(fileSystem.getPath(uri.pathOrThrow)).asSource().buffered()
    }

    actual fun openUriSink(uri: Uri, mode: String): Sink? =
        Files.newOutputStream(
            fileSystem.getPath(uri.pathOrThrow),
            *modeToOptions(mode).toTypedArray(),
        ).asSink().buffered()

    actual fun getImageWidthHeight(uri: Uri): Pair<Int?, Int?> =
        openUriSource(uri)?.use {
            it.asInputStream().use {
                uri.path?.substringAfterLast('.')
                    ?.let(ImageIO::getImageReadersBySuffix)
                    ?.forEach { reader ->
                        try {
                            FileCacheImageInputStream(it, null).use {
                                reader.setInput(it)
                                val width = reader.getWidth(reader.minIndex)
                                val height = reader.getHeight(reader.minIndex)
                                return width to height
                            }
                        } catch (_: Throwable) {
                        } finally {
                            reader.dispose()
                        }
                    }
                null to null
            }
        } ?: (null to null)

    // TODO
    actual fun getImageType(path: Path): String? = null

    actual fun writeEntryImage(outputPath: Path, imageUri: Uri?): Result<*>? {
        imageUri ?: return null
        try {
            openUriSource(imageUri).use { input ->
                input
                    ?: return Result.failure<Unit>(IllegalArgumentException("Could not open image"))
                Files.newOutputStream(
                    fileSystem.getPath(outputPath.toString()),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                ).use { output ->
                    input.transferTo(output.asSink())
                }
            }
        } catch (t: Throwable) {
            return Result.failure<Unit>(t)
        }
        return Result.success(Unit)
    }

    actual fun openEncryptedSource(path: Path) =
        Files.newInputStream(
            fileSystem.getPath(path.toString()),
            StandardOpenOption.READ,
        ).asSource().buffered()

    actual fun openEncryptedSink(path: Path) =
        Files.newOutputStream(
            fileSystem.getPath(path.toString()),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
        ).asSink().buffered()

    actual fun exists(path: Path) = Files.exists(fileSystem.getPath(path.toString()))

    actual fun createDirectories(path: Path) {
        Files.createDirectories(fileSystem.getPath(path.toString()))
    }

    actual fun delete(path: Path) {
        Files.delete(fileSystem.getPath(path.toString()))
    }

    actual fun metadataOrNull(path: Path): FileMetadata? {
        val filePath = fileSystem.getPath(path.toString())
        if (!Files.exists(filePath)) return null
        return FileMetadata(
            isRegularFile = Files.isRegularFile(filePath),
            isDirectory = Files.isDirectory(filePath),
            size = Files.size(filePath)
        )
    }

    actual fun sink(path: Path) = Files.newOutputStream(
        fileSystem.getPath(path.toString()),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
    ).asSink()

    actual fun list(path: Path): Collection<Path> =
        Files.list(fileSystem.getPath(path.toString())).map { Path(it.pathString) }.toList()

    actual fun atomicMove(source: Path, destination: Path) {
        Files.move(
            fileSystem.getPath(source.toString()),
            fileSystem.getPath(destination.toString()),
        )
    }

    actual fun source(path: Path) = Files.newInputStream(
        fileSystem.getPath(path.toString()),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
    ).asSource()

    private val Uri.pathOrThrow: String
        get() {
            if (scheme != "file" || path == null) {
                throw IllegalArgumentException("Platform only supports file:// URIs, got $this")
            }
            return path!!
        }

    private fun modeToOptions(mode: String): MutableList<OpenOption> {
        val options = mutableListOf<OpenOption>()
        if (mode.contains("w")) {
            options += StandardOpenOption.WRITE
            options += StandardOpenOption.CREATE
        }
        if (mode.contains("t")) options += StandardOpenOption.TRUNCATE_EXISTING
        if (mode.contains("r")) options += StandardOpenOption.READ
        return options
    }

    actual fun lastModifiedTime(path: Path) =
        Files.getLastModifiedTime(fileSystem.getPath(path.toString())).toInstant().toKotlinInstant()
}
