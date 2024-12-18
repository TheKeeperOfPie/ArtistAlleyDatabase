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
                "libs.room.paging",
            ).forEach(::api)
            libs.find(
                "libs.room.runtime",
            ).forEach(::runtimeOnly)
        }
    }
}

dependencies {
    kspProcessors.find(
        "kspProcessors.room.compiler",
    ).forEach {
        add("kspCommonMainMetadata", it)
    }
}
