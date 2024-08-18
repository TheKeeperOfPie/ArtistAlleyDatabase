plugins {
    id("library-android")
    id("library-compose")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.monetization.debug"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.hilt.android)
        }
        commonMain.dependencies {
            api(project(":modules:monetization"))
        }
    }
}

dependencies {
    add("kspAndroid", kspProcessors.hilt.compiler)
    add("kspAndroid", kspProcessors.androidx.hilt.compiler)
}
