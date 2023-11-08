
import gradle.kotlin.dsl.accessors._4a18f8e30691389e0583de30bf7ecc54.androidTestImplementation
import gradle.kotlin.dsl.accessors._4a18f8e30691389e0583de30bf7ecc54.androidTestRuntimeOnly

val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
dependencies {
    androidTestImplementation(project(":modules:test-utils"))
    androidTestImplementation(project(":modules:network-utils"))

    libs(
        "libs.dexmaker.mockito.inline.extended",
        "libs.androidx.junit.test",
        "libs.androidx.test.runner",
        "libs.junit.jupiter.api",
        "libs.junit5.android.test.core",
        "libs.kotlinx.coroutines.test",
    ).forEach(::androidTestImplementation)

    libs(
        "libs.junit.jupiter.engine",
        "libs.junit5.android.test.runner",
    ).forEach(::androidTestRuntimeOnly)
}

fun libs(vararg names: String) = names.map {
    try {
        libs.findLibrary(it.removePrefix("libs.")).get().get()
    } catch (t: Throwable) {
        throw IllegalArgumentException("Failed to find $it", t)
    }
}
