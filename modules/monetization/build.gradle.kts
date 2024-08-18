plugins {
    id("library-android")
    id("library-compose")
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.monetization"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.hilt.android)
        }
        commonMain.dependencies {
            api(project(":modules:utils"))
            api(project(":modules:utils-compose"))
        }
    }
}

dependencies {
    add("kspAndroid", kspProcessors.hilt.compiler)
    add("kspAndroid", kspProcessors.androidx.hilt.compiler)
}
