import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("com.android.library")
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

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        jvmToolchain(18)
    }
}
