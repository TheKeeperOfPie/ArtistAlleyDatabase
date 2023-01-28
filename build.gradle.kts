import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.44.2")
        classpath("de.mannodermaus.gradle.plugins:android-junit5:1.8.2.1")
    }
}

plugins {
    id("com.android.application") version "8.1.0-alpha02" apply false
    id("com.android.library") version "8.1.0-alpha02" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
    id("org.jetbrains.kotlin.jvm") version "1.8.0" apply false
    id("com.autonomousapps.dependency-analysis") version "1.18.0"
    id("com.github.ben-manes.versions") version "0.44.0"
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