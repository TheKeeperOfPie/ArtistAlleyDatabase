import java.io.File
import java.util.concurrent.TimeUnit

val assetsPath = "src/commonMain/composeResources/files"
val catalogsDir = __FILE__.resolve("../../$assetsPath/catalogs")
val ralliesDir = __FILE__.resolve("../../$assetsPath/rallies")

compressImages(catalogsDir)
deleteLarger(catalogsDir)

compressImages(ralliesDir)
deleteLarger(ralliesDir)

private fun compressImages(directory: File) {
    directory.walk()
        .filter { it.extension != "webp" }
        .filter { it.isFile }
        .forEach {
            val output = it.resolveSibling(it.nameWithoutExtension + ".webp")
            println("Processing ${readableName(it)}")
            ProcessBuilder(
                "cwebp",
                "-q",
                "80",
                "-m",
                "6",
                it.absolutePath,
                "-o",
                output.absolutePath,
            )
                .redirectErrorStream(true)
                .start()
                .waitFor(10, TimeUnit.SECONDS)
        }
}

private fun deleteLarger(directory: File) {
    directory.walk()
        .filter { it.extension != "webp" }
        .filter { it.isFile }
        .forEach { original ->
            val compressed = original.resolveSibling(original.nameWithoutExtension + ".webp")
            if (!compressed.exists()) return@forEach
            if (compressed.length() > original.length()) {
                println("${readableName(original)} was better: ${original.length()} -> ${compressed.length()}")
                compressed.delete()
            } else {
                original.delete()
            }
        }
}

private fun readableName(file: File) = file.path
    .substringAfter(assetsPath.replace("/", File.separator))
    .removePrefix(File.separator)
