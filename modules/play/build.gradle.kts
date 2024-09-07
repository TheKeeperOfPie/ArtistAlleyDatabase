plugins {
    id("library-android")
    id("library-compose")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.play"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(project(":modules:android-utils"))
            api(project(":modules:compose-utils"))
            api(project(":modules:monetization"))
            implementation(compose.materialIconsExtended)
            implementation(libs.hilt.android)
            implementation(libs.app.update.ktx)
            implementation(libs.billing.ktx)
        }
    }
}

dependencies {
    add("kspAndroid", kspProcessors.hilt.compiler)
    add("kspAndroid", kspProcessors.androidx.hilt.compiler)
}
