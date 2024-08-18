plugins {
    id("library-android")
    id("library-compose")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

secrets {
    propertiesFileName = "secrets.properties"
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.monetization.unity"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.hilt.android)
            implementation(libs.unity.ads)
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
