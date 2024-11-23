import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("library-compose")
    id("library-desktop")
    id("library-inject")
}

kotlin {
    jvm("desktop")
    sourceSets {
        commonMain.dependencies {
            commonMain.dependencies {
                implementation(projects.modules.anime)
                implementation(projects.modules.cds)
                implementation(projects.modules.markdown)
                implementation(projects.modules.media)
                implementation(projects.modules.monetization)
                implementation(projects.modules.utils)
                implementation(projects.modules.utilsNetwork)
                implementation(projects.modules.utilsRoom)
                implementation(libs.jetBrainsCompose.navigation.compose)
                implementation(libs.kermit)
                implementation(libs.lifecycle.viewmodel.compose)

                implementation(libs.room.ktx)
                implementation(libs.room.paging)
                runtimeOnly(libs.room.runtime)
            }
            desktopMain.dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.androidx.sqlite.bundled)
                implementation(libs.coil3.coil.compose)
                implementation(libs.coil3.coil.network.ktor3)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.uri.kmp)
            }
        }
    }
}

configurations.all {
    // A Room artifact is including the Android coroutines library,
    // need to exclude so that Swing is used instead
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
}

dependencies {
    add("kspDesktop", kspProcessors.kotlin.inject.compiler.ksp)
    add("kspDesktop", kspProcessors.room.compiler)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

compose.desktop {
    application {
        mainClass = "com.thekeeperofpie.artistalleydatabase.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "com.thekeeperofpie.artistalleydatabase"
            packageVersion = "0.0.1"
        }
    }
}
