import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // This module is just a workaround for the secrets plugin not working with KMP,
    // so it doesn't use the convention plugins
    id("com.android.library")
    kotlin("android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

secrets {
    propertiesFileName = "secrets.properties"
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anilist.secrets"
    compileSdk = 35

    defaultConfig {
        minSdk = 28
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_18
        jvmToolchain(18)
    }
}
