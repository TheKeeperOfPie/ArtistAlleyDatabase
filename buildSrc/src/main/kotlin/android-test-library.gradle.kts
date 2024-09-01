
import gradle.kotlin.dsl.accessors._b2937d1b40dda98f7678619569c6e850.androidTestImplementation
import gradle.kotlin.dsl.accessors._b2937d1b40dda98f7678619569c6e850.androidTestRuntimeOnly

val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
dependencies {
    androidTestImplementation(project(":modules:test-utils"))
    androidTestImplementation(project(":modules:utils-network"))

    libs.find(
        "libs.dexmaker.mockito.inline.extended",
        "libs.androidx.junit.test",
        "libs.androidx.test.runner",
        "libs.junit.jupiter.api",
        "libs.junit5.android.test.core",
        "libs.kotlinx.coroutines.test",
    ).forEach(::androidTestImplementation)

    libs.find(
        "libs.junit.jupiter.engine",
        "libs.junit5.android.test.runner",
    ).forEach(::androidTestRuntimeOnly)
}
