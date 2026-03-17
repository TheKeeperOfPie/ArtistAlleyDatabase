plugins {
    id("library-kotlin")
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            resolveLibraries("libs.room.paging").forEach(::api)
            resolveLibraries("libs.room.runtime").forEach(::implementation)
        }
    }
}

dependencies {
    resolveLibraries("libs.room.compiler").forEach {
        add("kspCommonMainMetadata", it)
    }
}
