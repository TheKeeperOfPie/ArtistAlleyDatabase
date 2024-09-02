plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":modules:anilist"))
            api(project(":modules:browse"))
            api(project(":modules:data"))
            api(project(":modules:entry"))
            api(project(":modules:musical-artists"))
            implementation(project(":modules:utils"))
            implementation(project(":modules:utils-room"))
            api(project(":modules:vgmdb"))

            implementation(libs.jetBrainsCompose.navigation.compose)
            implementation(libs.kermit)
            implementation(libs.coil3.coil.compose)

            api(libs.room.ktx)
            api(libs.room.paging)
            runtimeOnly(libs.room.runtime)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.cds"
}

dependencies {
    add("kspAndroid", kspProcessors.room.compiler)
}
