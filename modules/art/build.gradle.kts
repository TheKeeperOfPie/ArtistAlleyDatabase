plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.modules.anilist)
            api(projects.modules.browse)
            api(projects.modules.data)
            api(projects.modules.entry)
            implementation(projects.modules.utilsCompose)
            implementation(projects.modules.utilsRoom)

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
    namespace = "com.thekeeperofpie.artistalleydatabase.art"
}

dependencies {
    add("kspAndroid", kspProcessors.room.compiler)
}

compose.resources {
    publicResClass = true
}
