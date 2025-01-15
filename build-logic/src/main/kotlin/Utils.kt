import org.gradle.api.artifacts.VersionCatalog
import java.io.File
import java.util.zip.CRC32

object Utils {

    fun hash(file: File): Long {
        val crc32 = CRC32()
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                crc32.update(buffer, 0, bytesRead)
            }
        }
        return crc32.value
    }
}

fun VersionCatalog.find(vararg names: String) = names.map {
    try {
        findLibrary(it.removePrefix("libs.").removePrefix("kspProcessors.")).get().get()
    } catch (t: Throwable) {
        throw IllegalArgumentException("Failed to find $it", t)
    }
}
