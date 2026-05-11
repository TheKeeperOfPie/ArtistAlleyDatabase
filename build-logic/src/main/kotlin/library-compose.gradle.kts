plugins {
    id("library-desktop")
    id("library-kotlin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                resolveLibraries(
                    "libs.jetBrainsCompose.components.resources",
                    "libs.jetBrainsCompose.components.ui.tooling.preview",
                    "libs.jetBrainsCompose.material3",
                    "libs.jetBrainsCompose.foundation",
                    "libs.jetBrainsCompose.runtime",
                    "libs.jetBrainsCompose.ui",
                ).forEach(::implementation)
            }
        }
        commonTest.dependencies {
            resolveLibraries("libs.jetBrainsCompose.ui.test")
                .forEach(::implementation)
        }
        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                resolveLibraries("libs.jetBrainsCompose.ui.tooling")
                    .forEach(::implementation)
            }
        }
    }
}

composeCompiler {
    includeSourceInformation = true
}
