plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    id("library-room")
    alias(libs.plugins.com.apollographql.apollo3.external)
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

            implementation(libs.human.readable)
            implementation(libs.jetBrainsAndroidX.lifecycle.viewmodel.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.stately.concurrent.collections)
            implementation(libs.uri.kmp)
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

apollo {
    service("aniList") {
        packageName.set("com.anilist.data")
        dependsOn(project(":modules:anilist:data"))
        codegenModels.set("responseBased")
        decapitalizeFields.set(true)
        plugin(projects.modules.apollo)
    }
}
