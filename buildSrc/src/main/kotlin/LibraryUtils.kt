import org.gradle.api.artifacts.VersionCatalog

fun VersionCatalog.find(vararg names: String) = names.map {
    try {
        findLibrary(it.removePrefix("libs.")).get().get()
    } catch (t: Throwable) {
        throw IllegalArgumentException("Failed to find $it", t)
    }
}
