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
        kotlinCompilerExtensionVersion = "1.4.7-dev-k1.9.0-Beta-bb7dc8b44eb"
    }
}
