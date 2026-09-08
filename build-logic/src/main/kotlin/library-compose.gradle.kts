import com.github.takahirom.roborazzi.ExperimentalRoborazziApi

plugins {
    id("library-android")
    id("library-desktop")
    id("library-kotlin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("io.github.takahirom.roborazzi")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            if (project.path != ":modules:utils-preview") {
                implementation(project(":modules:utils-preview"))
            }
            resolveLibraries(
                "libs.jetBrainsCompose.components.resources",
                "libs.jetBrainsCompose.material3",
                "libs.jetBrainsCompose.foundation",
                "libs.jetBrainsCompose.runtime",
                "libs.jetBrainsCompose.ui",
                "libs.jetBrainsCompose.ui.tooling.preview",
            ).forEach(::implementation)
        }
        commonTest.dependencies {
            resolveLibraries("libs.jetBrainsCompose.ui.test")
                .forEach(::implementation)
        }
        getByName("androidMain").dependencies {
            resolveLibraries(
                "libs.jetBrainsCompose.ui.tooling",
            ).forEach(::implementation)
        }
        getByName("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
            resolveLibraries(
                "libs.jetBrainsCompose.ui.tooling",
            ).forEach(::implementation)
        }
        getByName("desktopTest").dependencies {
            implementation(project(":modules:utils-roborazzi"))
            resolveLibraries(
                "libs.roborazzi.compose.desktop.preview.scanner.support",
                "libs.composable.preview.scanner",
                "libs.junit",
            ).forEach(::implementation)
        }
    }
}

@OptIn(ExperimentalRoborazziApi::class)
roborazzi {
    outputDir = file("src/screenshotTest")
    separateOutputDirs = true
    generateComposePreviewDesktopTests {
        enable = true
        packages = listOf("*")
        includePrivatePreviews = true
        useScanOptionParametersInTester = true
        testerQualifiedClassName = "com.thekeeperofpie.artistalleydatabase.utils_roborazzi.WebpDesktopPreviewTester"
    }
}

composeCompiler {
    includeSourceInformation = true
}
