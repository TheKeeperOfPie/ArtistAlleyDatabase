@file:Suppress("UnstableApiUsage")

val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
val Project.kspProcessors: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("kspProcessors")
plugins {
    id("library-kotlin")
    id("library-desktop")
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":modules:utils-inject"))
            libs.find(
                "libs.kotlin.inject.runtime.kmp",
            ).forEach(::implementation)
        }
    }
}

dependencies {
    kspProcessors.find(
        "kspProcessors.kotlin.inject.compiler.ksp",
    ).forEach {
        add("kspCommonMainMetadata", it)

        // KSP doesn't work for commonTest, so all tests will be under desktopTest
        add("kspDesktopTest", it)
    }
}
