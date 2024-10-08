plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.modules.entry)
            api(projects.modules.utils)
            api(projects.modules.utilsCompose)
            api(projects.modules.utilsNetwork)

            implementation(libs.kermit)
            implementation(libs.ksoup)
            implementation(libs.ktor.client.core)
            implementation(libs.okhttp)

            runtimeOnly(libs.room.runtime)
            implementation(libs.room.ktx)
            implementation(libs.room.paging)
        }
        commonTest.dependencies {
            implementation(libs.junit)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.skrapeit)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.vgmdb"
}

dependencies {
    add("kspCommonMainMetadata", kspProcessors.room.compiler)
}

compose.resources {
    publicResClass = true
}
