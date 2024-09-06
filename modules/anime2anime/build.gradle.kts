plugins {
    id("library-android")
    id("library-compose")
    id("library-inject")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    alias(libs.plugins.de.mannodermaus.android.junit5)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":modules:anilist"))
            api(project(":modules:anime"))
            implementation(libs.hilt.android)
            implementation(libs.hilt.navigation.compose)

            implementation(libs.accompanist.flowlayout)
            implementation(libs.coil3.coil.compose)
            implementation(libs.kermit)
            implementation(libs.molecule.runtime)
            implementation(libs.paging.common)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime2anime"
}

dependencies {
    add("kspAndroid", kspProcessors.hilt.compiler)
    add("kspAndroid", kspProcessors.androidx.hilt.compiler)
}
