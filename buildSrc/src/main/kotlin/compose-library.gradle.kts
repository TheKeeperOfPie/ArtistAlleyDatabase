repositories {
    mavenCentral()
    google()
}

plugins {
    id("module-library")
    id("com.google.devtools.ksp")
}

android {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.4-dev-k1.8.20-RC-88d9f3a8232"
    }
}