@file:Suppress("UnstableApiUsage")

val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
val Project.kspProcessors: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("kspProcessors")
plugins {
    id("library-kotlin")
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            libs.find(
                "libs.kotlin.inject.runtime.kmp",
            ).forEach(::implementation)
        }
    }
}

dependencies {
    kspProcessors.find(
        "kspProcessors.kotlin-inject-compiler-ksp",
    ).forEach {
        add("kspCommonMainMetadata", it)
    }
}
