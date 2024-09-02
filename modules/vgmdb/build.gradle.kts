plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":modules:entry"))
            api(project(":modules:utils"))
            api(project(":modules:utils-compose"))
            api(project(":modules:utils-network"))

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
