plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-room")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(libs.androidx.security.crypto)
            implementation(projects.modules.secrets)
            implementation(libs.androidx.browser)
            implementation(libs.activity.compose)
        }
        commonMain.dependencies {
            api(projects.modules.anilist.data)
            api(projects.modules.entry)
            api(projects.modules.apollo.utils)
            implementation(projects.modules.utils)
            implementation(projects.modules.utilsCompose)
            implementation(projects.modules.utilsNetwork)

            implementation(libs.apollo.engine.ktor)
            implementation(libs.apollo.runtime)
            implementation(libs.apollo.normalized.cache)
            implementation(libs.apollo.normalized.cache.sqlite)

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

compose.resources {
    publicResClass = true
}
