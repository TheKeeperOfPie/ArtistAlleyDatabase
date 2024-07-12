
import gradle.kotlin.dsl.accessors._986422958230f3dfad63fcbc2d0a281d.androidTestImplementation
import gradle.kotlin.dsl.accessors._986422958230f3dfad63fcbc2d0a281d.androidTestRuntimeOnly

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
