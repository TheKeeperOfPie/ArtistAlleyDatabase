plugins {
    id("library-android")
    id("library-compose")
    id("library-inject")
    alias(libs.plugins.de.mannodermaus.android.junit5)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":modules:anilist"))
            api(project(":modules:anime"))

            implementation(libs.accompanist.flowlayout)
            implementation(libs.coil3.coil.compose)
            implementation(libs.kermit)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.molecule.runtime)
            implementation(libs.paging.common)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime2anime"
}
