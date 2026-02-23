import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_18
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.utils.buildconfig"
    compileSdk = 36
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }

    defaultConfig {
        minSdk = 28
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        create("internal")
    }
}
