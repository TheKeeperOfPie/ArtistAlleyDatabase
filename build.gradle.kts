import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.filter.LicenseBundleNormalizer
import com.github.jk1.license.render.InventoryHtmlReportRenderer
import com.github.jk1.license.render.ReportRenderer

plugins {
    id("com.google.dagger.hilt.android") version "2.45" apply false
    id("de.mannodermaus.android-junit5") version "1.8.2.1" apply false
    id("com.autonomousapps.dependency-analysis") version "1.19.0"
    id("com.github.ben-manes.versions") version "0.45.0"
    id("com.github.jk1.dependency-license-report") version "2.1"
    kotlin("plugin.serialization") version "1.8.10" apply false
}

licenseReport {
    renderers = arrayOf<ReportRenderer>(
        InventoryHtmlReportRenderer("report.html", "Backend")
    )
    filters = arrayOf<DependencyFilter>(LicenseBundleNormalizer())
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    checkForGradleUpdate = true
    outputFormatter = "html"
}

tasks.register("copyGitHooks", Copy::class) {
    from(File(rootProject.rootDir, "scripts/git/pre-commit"))
    into(File(rootProject.rootDir, ".git/hooks"))
    fileMode = 777
}

tasks.register("recopyVerificationMetadata") {
    File(rootProject.rootDir, "gradle/verification-metadata-base.xml")
        .copyTo(File(rootProject.rootDir, "gradle/verification-metadata.xml"), overwrite = true)
}

tasks.register("resetVerificationMetadata") {
    dependsOn("recopyVerificationMetadata")
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("build") })
    dependsOn("buildHealth")
}

dependencyAnalysis {
    dependencies {
        bundle("kotlin-stdlib") {
            includeGroup("org.jetbrains.kotlin")
        }
        bundle("androidx-room") {
            includeGroup("androidx.room")
        }
    }
    issues {
        all {
            onAny {
                severity("fail")
            }
            ignoreKtx(true)
            onUsedTransitiveDependencies {
                severity("ignore")
            }
            onUnusedDependencies {
                exclude(
                    "androidx.compose.ui:ui-tooling-preview",
                    "com.google.dagger:hilt-android",
                    "com.squareup.moshi:moshi-kotlin",
                )
            }
            onUnusedAnnotationProcessors {
                exclude(
                    "androidx.hilt:hilt-compiler",
                    "com.google.dagger:hilt-compiler",
                )
            }
        }

        project(":app") {
            onUnusedDependencies {
                exclude(
                    "androidx.compose.ui:ui-tooling",
                    "androidx.lifecycle:lifecycle-runtime-ktx",
                )
            }
        }

        project(":modules:browse") {
            onUnusedDependencies {
                exclude(
                    "androidx.navigation:navigation-compose",
                )
            }
        }

        project(":modules:musical-artists") {
            onUnusedDependencies {
                exclude(
                    "org.jetbrains.kotlinx:kotlinx-serialization-json",
                )
            }
        }
    }
}