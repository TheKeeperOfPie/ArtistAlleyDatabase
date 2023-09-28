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
        kotlinCompilerExtensionVersion = "1.5.4-dev-k1.9.20-Beta2-ac5f960bdaf"
    }
}
