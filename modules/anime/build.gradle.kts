plugins {
    id("library-android")
    id("library-compose")
    id("library-desktop")
    id("library-inject")
    alias(libs.plugins.de.mannodermaus.android.junit5)
}

kotlin {
    sourceSets {
        androidInstrumentedTest.dependencies {
            implementation(libs.junit.jupiter.params)
            implementation(libs.junit5.android.test.compose)
        }
        commonMain.dependencies {
            api(projects.modules.anilist)
            api(projects.modules.anime.data)
            api(projects.modules.anime.news)
            api(projects.modules.cds)
            api(projects.modules.markdown)
            api(projects.modules.media)
            api(projects.modules.monetization)
            api(projects.modules.utilsCompose)
            api(projects.modules.utilsNetwork)

            api(libs.room.ktx)
            api(libs.room.paging)
            runtimeOnly(libs.room.runtime)

            implementation(libs.coil3.coil.compose)
            implementation(libs.compose.placeholder.material3)
            implementation(libs.fluid.country)
            implementation(libs.fluid.i18n)
            implementation(libs.htmlconverter)
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
}

compose.resources {
    publicResClass = true
}
