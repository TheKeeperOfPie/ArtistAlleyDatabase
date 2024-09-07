plugins {
    id("library-android")
    id("library-compose")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.monetization.unity"

    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(project(":modules:monetization"))
            implementation(project(":modules:secrets"))
            implementation(libs.hilt.android)
            implementation(libs.unity.ads)
        }
    }
}

dependencies {
    add("kspAndroid", kspProcessors.hilt.compiler)
    add("kspAndroid", kspProcessors.androidx.hilt.compiler)
}
