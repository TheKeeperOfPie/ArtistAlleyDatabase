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
        androidInstrumentedTest.dependencies {
            implementation(libs.junit.jupiter.params)
            implementation(libs.junit5.android.test.compose)
            runtimeOnly(libs.compose.ui.test.manifest)
        }
        commonMain.dependencies {
            api(project(":modules:anilist"))
            api(project(":modules:cds"))
            api(project(":modules:markdown"))
            api(project(":modules:media"))
            api(project(":modules:monetization"))
            api(project(":modules:news"))
            api(project(":modules:utils-compose"))

            api(libs.room.ktx)
            api(libs.room.paging)
            runtimeOnly(libs.room.runtime)
            implementation(libs.hilt.android)
            implementation(libs.hilt.navigation.compose)

            implementation(libs.coil3.coil.compose)
            implementation(libs.compose.placeholder.material3)
            implementation(libs.fluid.country)
            implementation(libs.fluid.i18n)
            implementation(libs.human.readable)
            implementation(libs.jetBrainsCompose.navigation.compose)
            implementation(libs.kermit)
            implementation(libs.kotlinx.serialization.json.io)
            implementation(libs.stately.concurrent.collections)
        }
    }
}

android {
    namespace = "com.thekeeperofpie.artistalleydatabase.anime"
}

dependencies {
    add("kspAndroid", kspProcessors.room.compiler)
    add("kspAndroid", kspProcessors.hilt.compiler)
    add("kspAndroid", kspProcessors.androidx.hilt.compiler)
}
