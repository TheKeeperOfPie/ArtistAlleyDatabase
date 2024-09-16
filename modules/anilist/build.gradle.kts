plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(libs.androidx.security.crypto)
            implementation(project(":modules:secrets"))
            implementation(libs.androidx.browser)
            implementation(libs.activity.compose)
        }
        commonMain.dependencies {
            api(project(":modules:anilist-data"))
            api(project(":modules:entry"))
            implementation(project(":modules:apollo:utils"))
            implementation(project(":modules:utils"))
            implementation(project(":modules:utils-compose"))
            implementation(project(":modules:utils-network"))

            api(libs.room.ktx)
            api(libs.room.paging)
            runtimeOnly(libs.room.runtime)

            implementation(libs.apollo.engine.ktor)
            implementation(libs.apollo.runtime)
            implementation(libs.apollo.normalized.cache)
            implementation(libs.apollo.normalized.cache.sqlite)

            implementation(libs.kermit)
            implementation(libs.ktor.client.core)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.uri.kmp)
            implementation(libs.human.readable)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anilist"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

dependencies {
    add("kspAndroid", kspProcessors.room.compiler)
}

compose.resources {
    publicResClass = true
}
