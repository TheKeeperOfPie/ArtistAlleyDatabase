package com.thekeeperofpie.artistalleydatabase.utils.io

import com.eygraber.uri.Uri
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import kotlinx.datetime.toKotlinInstant
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.FileMetadata
import kotlinx.io.files.Path
import me.tatarka.inject.annotations.Inject
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.pathString

@SingletonScope
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

    actual fun openUriSource(uri: Uri): Source? =
        Files.newInputStream(fileSystem.getPath(uri.pathOrThrow)).asSource().buffered()

    actual fun openUriSink(uri: Uri, mode: String): Sink? =
        Files.newOutputStream(
            fileSystem.getPath(uri.pathOrThrow),
            *modeToOptions(mode).toTypedArray(),
        ).asSink().buffered()

    actual fun getImageWidthHeight(
        uri: Uri,
    ): Pair<Int?, Int?> {
        // TODO
        return null to null
    }

    actual fun getImageType(
        path: Path,
    ): String? {
        // TODO
        return null
    }

    actual fun writeEntryImage(
        outputPath: Path,
        imageUri: Uri?,
    ): Result<*>? {
        // TODO
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
                throw IllegalArgumentException("Platform only supports file:// URIs")
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
