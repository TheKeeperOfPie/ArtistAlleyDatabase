plugins {
    id("library-desktop")
    id("io.github.takahirom.roborazzi")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    sourceSets {
        getByName("desktopMain").dependencies {
            implementation(libs.composable.preview.scanner)
            implementation(libs.jetBrainsCompose.runtime)
            implementation(libs.jetBrainsCompose.ui.test)
            implementation(libs.roborazzi.compose.desktop.preview.scanner.support)
            implementation(libs.webp.imageio)
        }
    }
}
